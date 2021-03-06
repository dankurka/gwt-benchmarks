/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.benchmark.compileserver.shared;

import com.google.gwt.benchmark.common.shared.service.ServiceException;
import com.google.gwt.benchmark.compileserver.shared.dto.BenchmarkOverviewResponseDTO;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Service interface implemented server side.
 */
@RemoteServiceRelativePath("data/service")
public interface Service extends RemoteService {

  BenchmarkOverviewResponseDTO loadBenchmarkOverview() throws ServiceException;

  void startServer();

  void stopServer();
}
