/*
 * Copyright © 2024 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cdap.wrangler.api.parser;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Token implementation for time duration values like "5ms", "2.5s", "1m".
 */
public class TimeDuration implements Token {
  private final double value;
  private final String unit;

  public TimeDuration(String input) {
    if (input == null || input.isEmpty()) {
      throw new IllegalArgumentException("Time duration input cannot be null or empty");
    }

    this.unit = input.replaceAll("[0-9.]", "").toLowerCase();
    String number = input.replaceAll("[^0-9.]", "");

    if (number.isEmpty() || unit.isEmpty()) {
      throw new IllegalArgumentException("Invalid time duration format: " + input);
    }

    this.value = Double.parseDouble(number);
  }

  public long getMilliseconds() {
    switch (unit) {
      case "ms": return (long) value;
      case "s":  return (long) (value * 1000);
      case "m":  return (long) (value * 60 * 1000);
      case "h":  return (long) (value * 60 * 60 * 1000);
      case "d":  return (long) (value * 24 * 60 * 60 * 1000);
      default: throw new IllegalArgumentException("Unsupported time unit: " + unit);
    }
  }

  @Override
  public Object value() {
    return getMilliseconds();
  }

  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }

  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(getMilliseconds());
  }
}
