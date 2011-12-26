/*
 * Copyright (C) 2011 The Common Platform Team, KTH, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.keun.android.common.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES를 이용한 암호화.
 * 
 * @author Keun-yang Son
 * @since 2011. 12. 8.
 * @version 1.0
 */
public class AES {
    private final Cipher mCipher;
    private final SecretKeySpec mKey;
    private final IvParameterSpec mIv;

    /**
     * Creates a StringEncrypter instance.
     * 
     * @param key A key string which is converted into UTF-8 and hashed by MD5.
     *            Null or an empty string is not allowed.
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public AES(final String key) throws NoSuchAlgorithmException, NoSuchPaddingException,
            UnsupportedEncodingException {
        if (key == null || "".equals(key)) {
            throw new NullPointerException("The key can not be null or an empty string..");
        }
        mCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        // Initialize an encryption key and an initial vector.
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        this.mKey = new SecretKeySpec(md5.digest(key.getBytes("UTF-8")), "AES");
        this.mIv = null;
    }

    /**
     * Creates a StringEncrypter instance.
     * 
     * @param key A key string which is converted into UTF-8 and hashed by MD5.
     *            Null or an empty string is not allowed.
     * @param iv An initial vector string which is converted into UTF-8 and
     *            hashed by MD5. Null or an empty string is not allowed.
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public AES(final String key, final String iv) throws NoSuchAlgorithmException,
            NoSuchPaddingException, UnsupportedEncodingException {
        if (key == null || "".equals(key)) {
            throw new NullPointerException("The key can not be null or an empty string..");
        }
        if (iv == null || "".equals(iv)) {
            throw new NullPointerException(
                    "The initial vector can not be null or an empty string..");
        }
        mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Initialize an encryption key and an initial vector.
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        this.mKey = new SecretKeySpec(md5.digest(key.getBytes("UTF-8")), "AES");
        this.mIv = new IvParameterSpec(md5.digest(iv.getBytes("UTF-8")));
    }

    /**
     * Encrypts a string.
     * 
     * @param value A string to encrypt. It is converted into UTF-8 before being
     *            encrypted. Null is regarded as an empty string.
     * @return An encrypted string.
     * @throws Exception
     */
    public String encrypt(final String value) throws Exception {
        if (value == null || "".equals(value)) {
            throw new NullPointerException("The cipher string can not be null or an empty string..");
        }

        // Initialize the cryptography algorithm.
        if (mIv == null || "".equals(mIv)) {
            mCipher.init(Cipher.ENCRYPT_MODE, mKey);
        } else {
            mCipher.init(Cipher.ENCRYPT_MODE, mKey, mIv);
        }

        // Get a UTF-8 byte array from a unicode string.
        byte[] utf8Value = value.getBytes("UTF-8");

        // Encrypt the UTF-8 byte array.
        byte[] encryptedValue = mCipher.doFinal(utf8Value);

        // Return a base64 encoded string of the encrypted byte array.
        return new String(Base64.encode(encryptedValue, Base64.DEFAULT));
    }

    /**
     * Decrypts a string which is encrypted with the same key and initial
     * vector.
     * 
     * @param value A string to decrypt. It must be a string encrypted with the
     *            same key and initial vector. Null or an empty string is not
     *            allowed.
     * @return A decrypted string
     * @throws Exception
     */
    public String decrypt(final String value) throws Exception {
        if (value == null || "".equals(value)) {
            throw new NullPointerException("The cipher string can not be null or an empty string.");
        }

        // Initialize the cryptography algorithm.
        if (mIv == null || "".equals(mIv)) {
            mCipher.init(Cipher.DECRYPT_MODE, mKey);
        } else {
            mCipher.init(Cipher.DECRYPT_MODE, mKey, mIv);
        }

        // Get an encrypted byte array from a base64 encoded string.
        byte[] encryptedValue = Base64.decode(value.getBytes(), Base64.DEFAULT);

        // Decrypt the byte array.
        byte[] decryptedValue = mCipher.doFinal(encryptedValue);

        // Return a string converted from the UTF-8 byte array.
        return new String(decryptedValue, "UTF-8");
    }
}
