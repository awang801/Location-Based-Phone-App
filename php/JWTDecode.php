<?php
/**
 * Decodes a JWT string into a PHP object.
 *
 * @param string        $jwt            The JWT
 * @param string|array  $key            The key, or map of keys.
 *                                      If the algorithm used is asymmetric, this is the public key
 * @param array         $allowed_algs   List of supported verification algorithms
 *                                      Supported algorithms are 'HS256', 'HS384', 'HS512' and 'RS256'
 *
 * @return object The JWT's payload as a PHP object
 *
 * @throws UnexpectedValueException     Provided JWT was invalid
 * @throws SignatureInvalidException    Provided JWT was invalid because the signature verification failed
 * @throws BeforeValidException         Provided JWT is trying to be used before it's eligible as defined by 'nbf'
 * @throws BeforeValidException         Provided JWT is trying to be used before it's been created as defined by 'iat'
 * @throws ExpiredException             Provided JWT has since expired, as defined by the 'exp' claim
 *
 * @uses jsonDecode
 * @uses urlsafeB64Decode
 */
function decode($jwt, $key, $allowed_algs = array())
{

    /**
     * Allow the current timestamp to be specified.
     * Useful for fixing a value within unit testing.
     *
     * Will default to PHP time() value if null.
     */
    $timestamp = null;

    $supported_algs = array(
        'HS256' => array('hash_hmac', 'SHA256'),
        'HS512' => array('hash_hmac', 'SHA512'),
        'HS384' => array('hash_hmac', 'SHA384'),
        'RS256' => array('openssl', 'SHA256'),
    );

    /**
     * When checking nbf, iat or expiration times,
     * we want to provide some extra leeway time to
     * account for clock skew.
     */
    $leeway = 86400*3; //days worth of seconds * number of days


    $timestamp = is_null($timestamp) ? time() : $timestamp;

    if (empty($key)) {
        //echo 'Key may not be empty';
        return NULL;
    }
    if (!is_array($allowed_algs)) {
        //echo 'Algorithm not allowed';
        return NULL;
    }
    $tks = explode('.', $jwt);
    if (count($tks) != 3) {
        //echo 'Wrong number of segments';
        return NULL;
    }
    list($headb64, $bodyb64, $cryptob64) = $tks;
    if (null === ($header = jsonDecode(urlsafeB64Decode($headb64)))) {
        //echo 'Invalid header encoding';
        return NULL;
    }
    if (null === $payload = jsonDecode(urlsafeB64Decode($bodyb64))) {
        //echo 'Invalid claims encoding';
        return NULL;
    }
    $sig = urlsafeB64Decode($cryptob64);

    if (empty($header->alg)) {
        //echo 'Empty algorithm';
        return NULL;
    }
    if (empty($supported_algs[$header->alg])) {
        //echo 'Algorithm not supported';
        return NULL;
    }
    if (!in_array($header->alg, $allowed_algs)) {
        //echo 'Algorithm not allowed';
        return NULL;
    }
    if (is_array($key) || $key instanceof \ArrayAccess) {
        if (isset($header->kid)) {
            $key = $key[$header->kid];
        } else {
            //echo '"kid" empty, unable to lookup correct key';
            return NULL;
        }
    }

    // Check the signature
    if (!verify("$headb64.$bodyb64", $sig, $key, $header->alg)) {
        //echo 'Signature verification failed';
        return NULL;
    }

    // Check if the nbf if it is defined. This is the time that the
    // token can actually be used. If it's not yet that time, abort.
    if (isset($payload->nbf) && $payload->nbf > ($timestamp + $leeway)) {
        //echo 'Cannot handle token prior to ';
        return NULL;
    }

    // Check that this token has been created before 'now'. This prevents
    // using tokens that have been created for later use (and haven't
    // correctly used the nbf claim).
    if (isset($payload->iat) && $payload->iat > ($timestamp + $leeway)) {
        //echo 'Cannot handle token prior to ';
        return NULL;
    }

    // Check if this token has expired.
    if (isset($payload->exp) && ($timestamp - $leeway) >= $payload->exp) {
        //echo 'Expired token';
        return NULL;
    }

    return $payload;
}

/**
 * Verify a signature with the message, key and method. Not all methods
 * are symmetric, so we must have a separate verify and sign method.
 *
 * @param string            $msg        The original message (header and body)
 * @param string            $signature  The original signature
 * @param string|resource   $key        For HS*, a string key works. for RS*, must be a resource of an openssl public key
 * @param string            $alg        The algorithm
 *
 * @return bool
 *
 * @throws DomainException Invalid Algorithm or OpenSSL failure
 */
function verify($msg, $signature, $key, $alg)
{
  $supported_algs = array(
      'HS256' => array('hash_hmac', 'SHA256'),
      'HS512' => array('hash_hmac', 'SHA512'),
      'HS384' => array('hash_hmac', 'SHA384'),
      'RS256' => array('openssl', 'SHA256'),
  );

    if (empty($supported_algs[$alg])) {
        //echo 'Algorithm not supported';
        return NULL;
    }

    list($function, $algorithm) = $supported_algs[$alg];
    switch($function) {
        case 'openssl':
            $success = openssl_verify($msg, $signature, $key, $algorithm);
            if (!$success) {
                //echo "OpenSSL unable to verify data: ";
                return NULL;
            } else {
                return $signature;
            }
        case 'hash_hmac':
        default:
            $hash = hash_hmac($algorithm, $msg, $key, true);
            if (function_exists('hash_equals')) {
                return hash_equals($signature, $hash);
            }
            $len = min(safeStrlen($signature), safeStrlen($hash));

            $status = 0;
            for ($i = 0; $i < $len; $i++) {
                $status |= (ord($signature[$i]) ^ ord($hash[$i]));
            }
            $status |= (safeStrlen($signature) ^ safeStrlen($hash));

            return ($status === 0);
    }
}

/**
 * Decode a JSON string into a PHP object.
 *
 * @param string $input JSON string
 *
 * @return object Object representation of JSON string
 *
 * @throws DomainException Provided string was invalid JSON
 */
function jsonDecode($input)
{
    if (version_compare(PHP_VERSION, '5.4.0', '>=') && !(defined('JSON_C_VERSION') && PHP_INT_SIZE > 4)) {
        /** In PHP >=5.4.0, json_decode() accepts an options parameter, that allows you
         * to specify that large ints (like Steam Transaction IDs) should be treated as
         * strings, rather than the PHP default behaviour of converting them to floats.
         */
        $obj = json_decode($input, false, 512, JSON_BIGINT_AS_STRING);
    } else {
        /** Not all servers will support that, however, so for older versions we must
         * manually detect large ints in the JSON string and quote them (thus converting
         *them to strings) before decoding, hence the preg_replace() call.
         */
        $max_int_length = strlen((string) PHP_INT_MAX) - 1;
        $json_without_bigints = preg_replace('/:\s*(-?\d{'.$max_int_length.',})/', ': "$1"', $input);
        $obj = json_decode($json_without_bigints);
    }

    if (function_exists('json_last_error') && $errno = json_last_error()) {
        handleJsonError($errno);
    } elseif ($obj === null && $input !== 'null') {
        //echo 'Null result with non-null input';
        return NULL;
    }
    return $obj;
}

/**
 * Helper method to create a JSON error.
 *
 * @param int $errno An error number from json_last_error()
 *
 * @return void
 */
function handleJsonError($errno)
{
    $messages = array(
        JSON_ERROR_DEPTH => 'Maximum stack depth exceeded',
        JSON_ERROR_CTRL_CHAR => 'Unexpected control character found',
        JSON_ERROR_SYNTAX => 'Syntax error, malformed JSON'
    );
     //echo 'Unknown JSON error: ';
     return NULL;
}

/**
 * Get the number of bytes in cryptographic strings.
 *
 * @param string
 *
 * @return int
 */
function safeStrlen($str)
{
    if (function_exists('mb_strlen')) {
        return mb_strlen($str, '8bit');
    }
    return strlen($str);
}

/**
 * Decode a string with URL-safe Base64.
 *
 * @param string $input A Base64 encoded string
 *
 * @return string A decoded string
 */
function urlsafeB64Decode($input)
{
    $remainder = strlen($input) % 4;
    if ($remainder) {
        $padlen = 4 - $remainder;
        $input .= str_repeat('=', $padlen);
    }
    return base64_decode(strtr($input, '-_', '+/'));
}
?>
