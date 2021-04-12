/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.okhttp.v3_0

import io.opentelemetry.instrumentation.test.base.HttpClientTest
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.internal.http.HttpMethod
import spock.lang.Shared

abstract class AbstractOkHttp3Test extends HttpClientTest<Request> {

  abstract OkHttpClient.Builder configureClient(OkHttpClient.Builder clientBuilder)

  @Shared
  def client = configureClient(
    new OkHttpClient.Builder()
      .connectTimeout(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
      .retryOnConnectionFailure(false))
    .build()

  @Override
  Request buildRequest(String method, URI uri, Map<String, String> headers) {
    def body = HttpMethod.requiresRequestBody(method) ? RequestBody.create(MediaType.parse("text/plain"), "") : null
    return new Request.Builder()
      .url(uri.toURL())
      .method(method, body)
      .headers(Headers.of(headers)).build()
  }

  @Override
  int sendRequest(Request request, String method, URI uri, Map<String, String> headers) {
    return client.newCall(request).execute().code()
  }

  @Override
  void sendRequestWithCallback(Request request, String method, URI uri, Map<String, String> headers, Consumer<Integer> callback) {
    client.newCall(request).enqueue(new Callback() {
      @Override
      void onFailure(Call call, IOException e) {
        throw e
      }

      @Override
      void onResponse(Call call, Response response) throws IOException {
        callback.accept(response.code())
      }
    })
  }

  @Override
  boolean testRedirects() {
    false
  }

  @Override
  boolean testCausality() {
    false
  }
}
