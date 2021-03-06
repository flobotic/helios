/*-
 * -\-\-
 * Helios Client
 * --
 * Copyright (C) 2016 Spotify AB
 * --
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
 * -/-/-
 */

package com.spotify.helios.common.descriptors;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Throwables;
import com.spotify.helios.common.Json;
import java.io.IOException;

public abstract class Descriptor {

  public String toJsonString() {
    return Json.asStringUnchecked(this);
  }

  public byte[] toJsonBytes() {
    try {
      return Json.asBytes(this);
    } catch (JsonProcessingException e) {
      throw Throwables.propagate(e);
    }
  }

  public static <T extends Descriptor> T parse(final byte[] bytes, Class<T> clazz)
      throws IOException {
    return Json.read(bytes, clazz);
  }

  public static <T extends Descriptor> T parse(final String value, Class<T> clazz)
      throws IOException {
    return parse(value.getBytes(UTF_8), clazz);
  }
}
