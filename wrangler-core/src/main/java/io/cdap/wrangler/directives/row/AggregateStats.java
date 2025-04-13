/*
 * Copyright © 2024 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cdap.wrangler.directives.row;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;

import java.util.Collections;
import java.util.List;

/**
 * Directive that aggregates byte size and time duration from each row.
 * Outputs total byte size in MB and total duration in seconds.
 */
public class AggregateStats implements Directive {
  private String byteCol;
  private String timeCol;
  private String byteOutCol;
  private String timeOutCol;

  private long totalBytes;
  private long totalMillis;

  @Override
  public void initialize(Arguments args) {
    byteCol = ((Text) args.value("source_byte_column")).value();
    timeCol = ((Text) args.value("source_time_column")).value();
    byteOutCol = ((Text) args.value("output_byte_column")).value();
    timeOutCol = ((Text) args.value("output_time_column")).value();
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) {
    totalBytes = 0;
    totalMillis = 0;

    for (Row row : rows) {
      try {
        Object byteVal = row.getValue(byteCol);
        Object timeVal = row.getValue(timeCol);

        if (byteVal instanceof String && !((String) byteVal).isEmpty()) {
          totalBytes += new ByteSize((String) byteVal).getBytes();
        }

        if (timeVal instanceof String && !((String) timeVal).isEmpty()) {
          totalMillis += new TimeDuration((String) timeVal).getMilliseconds();
        }

      } catch (Exception e) {
        // Logging optional: skip malformed rows
      }
    }

    double totalMB = totalBytes / (1024.0 * 1024);
    double totalSeconds = totalMillis / 1000.0;

    Row result = new Row();
    result.add(byteOutCol, totalMB);
    result.add(timeOutCol, totalSeconds);

    return Collections.singletonList(result);
  }

  // ✅ Required by Directive (inherits Executor)
  @Override
  public void destroy() {
    // Clean up if needed (no-op here)
  }

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
    builder.define("source_byte_column", TokenType.COLUMN_NAME);
    builder.define("source_time_column", TokenType.COLUMN_NAME);
    builder.define("output_byte_column", TokenType.COLUMN_NAME);
    builder.define("output_time_column", TokenType.COLUMN_NAME);
    return builder.build();
  }
}
