
package com.saltar;

import com.saltar.http.Response;

public interface Callback<T> {

  /** Successful HTTP response. */
  void onSuccess(T t);

  /**
   * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
   * exception.
   * @param error
   */
  void onFail(Exception error);
}
