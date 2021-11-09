/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zodac.folding.api;

/**
 * Simple interface for POJOs are returned from the service.
 */
public interface ResponsePojo {

    /**
     * Returns the ID of the {@link ResponsePojo}.
     *
     * @return the ID
     */
    int getId();

    /**
     * Checks if the input {@link RequestPojo} is equal to the {@link ResponsePojo}.
     *
     * <p>
     * While the {@link RequestPojo} will likely not be a complete match, there should be enough fields to verify
     * if it is the same as an existing {@link ResponsePojo}.
     *
     * @param inputRequest input {@link RequestPojo}
     * @return <code>true</code> if the input{@link RequestPojo} is equal to the {@link ResponsePojo}
     */
    boolean isEqualRequest(final RequestPojo inputRequest);
}
