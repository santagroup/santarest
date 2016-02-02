package com.santarest.callback;

import com.santarest.SantaRest;

public interface Callback<A> {

    /**
     * Successful HTTP response.
     */
    void onSuccess(SantaRest.ActionState<A> state);

    /**
     * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
     * exception.
     *
     * @param error
     */
    void onFail(SantaRest.ActionState<A> state, Exception error);
}
