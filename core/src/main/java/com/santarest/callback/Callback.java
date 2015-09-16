package com.santarest.callback;

public interface Callback<A> {

    /**
     * Successful HTTP response.
     */
    void onSuccess(A action);

    /**
     * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
     * exception.
     *
     * @param error
     */
    void onFail(A action, Exception error);
}
