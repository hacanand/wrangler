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
package io.cdap.wrangler.directives.row;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransientStore;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.cdap.etl.api.Lookup;
import io.cdap.cdap.etl.api.StageMetrics;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Unit test for AggregateStats directive.
 */
public class AggregateStatsTest {

  @Test
  public void testAggregateStats() {
    AggregateStats directive = new AggregateStats();

    Arguments args = new Arguments() {
      @Override
      public Text value(String name) {
        if ("source_byte_column".equals(name)) {
          return new Text("data_size");
        } else if ("source_time_column".equals(name)) {
          return new Text("time_taken");
        } else if ("output_byte_column".equals(name)) {
          return new Text("total_size_mb");
        } else if ("output_time_column".equals(name)) {
          return new Text("total_time_sec");
        }
        throw new IllegalArgumentException("Unexpected argument: " + name);
      }

      @Override public int size() { return 4; }
      @Override public boolean contains(String name) { return true; }
      @Override public io.cdap.wrangler.api.parser.TokenType type(String name) { return null; }
      @Override public int line() { return 0; }
      @Override public int column() { return 0; }
      @Override public String source() { return ""; }
      @Override public com.google.gson.JsonElement toJson() { return null; }
    };

    directive.initialize(args);

    List<Row> rows = Arrays.asList(
      new Row("data_size", "2MB").add("time_taken", "5s"),
      new Row("data_size", "1MB").add("time_taken", "3s")
    );

ExecutorContext context = new ExecutorContext() {
  @Override public Environment getEnvironment() { return Environment.TESTING; }
  @Override public String getNamespace() { return "test"; }
  @Override public StageMetrics getMetrics() { return null; }
  @Override public String getContextName() { return "testContext"; }
  @Override public Map<String, String> getProperties() { return Collections.emptyMap(); }
  @Override public URL getService(String appId, String serviceId) { return null; }
  @Override public TransientStore getTransientStore() { return null; }

  
  public <T> Lookup<T> provide(String name) {
    return null;
  }

  @Override
  public <T> Lookup<T> provide(String name, Map<String, String> arguments) {
    return null;
  }
};

    List<Row> result = directive.execute(rows, context);
    Assert.assertEquals(1, result.size());

    Row output = result.get(0);
    Assert.assertEquals(3.0, (Double) output.getValue("total_size_mb"), 0.01);
    Assert.assertEquals(8.0, (Double) output.getValue("total_time_sec"), 0.01);
  }
}
