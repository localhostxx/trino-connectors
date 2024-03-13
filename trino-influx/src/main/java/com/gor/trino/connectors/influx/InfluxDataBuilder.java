package com.gor.trino.connectors.influx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import io.airlift.slice.Slice;
import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.predicate.Domain;
import io.trino.spi.predicate.Range;
import io.trino.spi.predicate.TupleDomain;
import io.trino.spi.type.VarcharType;

public class InfluxDataBuilder {

    public static Iterator<List<Object>> getTableDataIterator(String tableName, List<InfluxColumnHandle> columnHandles,
            TupleDomain<ColumnHandle> constraint) {
        InfluxDB client = InfluxCache.getClient();

        String queryString = queryBuilder(tableName, columnHandles);
        queryString = appendConstraintToQuery(queryString, tableName, constraint);

        System.out.println("Query: " + queryString);
        QueryResult queryResult = client.query(new Query(queryString));

        QueryResult.Result result = queryResult.getResults().get(0);
        List<QueryResult.Series> series = result.getSeries();
        if (series != null) {
            for (QueryResult.Series serie : series) {
                List<List<Object>> values = serie.getValues();

                if (values != null) {
                    return values.iterator();
                }
            }
        }

        return null;
    }

    private static String queryBuilder(String tableName, List<InfluxColumnHandle> columnHandles) {

        ArrayList<String> coloumnNames = new ArrayList<>();

        for (InfluxColumnHandle columnHandle : columnHandles) {
            coloumnNames.add(InfluxCache.getColoumnsCaseSensitiveNames().get(tableName)
                    .get(columnHandle.getColumnName()));
        }

        String selectColumns = String.join(", ",
                coloumnNames.stream().map(column -> "\"" + column + "\"").toArray(String[]::new));

        String query = String.format("SELECT %s FROM \"%s\"", selectColumns, tableName);

        return query;
    }

    private static String appendConstraintToQuery(String query, String tableName,
            TupleDomain<ColumnHandle> constraint) {
        if (constraint == null || constraint.isAll() || constraint.getDomains().isEmpty())
            return query + " LIMIT 1000";

        System.out.println("Constraint: " + constraint.getDomains());

        query += " WHERE ";

        for (Map.Entry<ColumnHandle, Domain> entry : constraint.getDomains().get().entrySet()) {
            InfluxColumnHandle influxColumnHandle = (InfluxColumnHandle) entry.getKey();
            Domain domain = entry.getValue();
            String columnName = InfluxCache.getColoumnsCaseSensitiveNames().get(tableName)
                    .get(influxColumnHandle.getColumnName());

            if (domain.isSingleValue()) {
                query = singleValueConstraint(columnName, influxColumnHandle, domain, query);
            } else {
                query = rangeValueConstraint(columnName, influxColumnHandle, domain, query);
            }
        }

        if (query.endsWith(" AND ")) {
            query = query.substring(0, query.length() - 5);
        }

        return query;
    }

    private static String singleValueConstraint(String columnName, InfluxColumnHandle influxColumnHandle,
            Domain domain, String query) {
        if (influxColumnHandle.getColumnType().equals(VarcharType.VARCHAR)) {
            query += String.format("\"%s\" = '%s' AND ", columnName,
                    ((Slice) domain.getSingleValue()).toStringUtf8());
        } else {
            query += String.format("\"%s\" = %s AND ", columnName,
                    domain.getSingleValue());
        }

        return query;
    }

    private static String rangeValueConstraint(String columnName, InfluxColumnHandle influxColumnHandle,
            Domain domain, String query) {

        System.out.println("Range: " + domain.getValues().getRanges());
        for (Range range : domain.getValues().getRanges().getOrderedRanges()) {
            if (range.isHighUnbounded()) {
                if (range.isLowInclusive()) {
                    query += String.format("\"%s\" >= %s AND ", columnName,
                            range.getLowBoundedValue());
                } else {
                    query += String.format("\"%s\" > %s AND ", columnName,
                            range.getLowBoundedValue());
                }
            } else if (range.isLowUnbounded()) {
                if (range.isHighInclusive()) {
                    query += String.format("\"%s\" <= %s AND ", columnName,
                            range.getHighBoundedValue());
                } else {
                    query += String.format("\"%s\" < %s AND ", columnName,
                            range.getHighBoundedValue());
                }

            } else {
                if (range.isLowInclusive()) {
                    query += String.format("\"%s\" >= %s AND ", columnName,
                            range.getLowBoundedValue());
                } else {
                    query += String.format("\"%s\" > %s AND ", columnName,
                            range.getLowBoundedValue());
                }

                if (range.isHighInclusive()) {
                    query += String.format("\"%s\" <= %s AND ", columnName,
                            range.getHighBoundedValue());
                } else {
                    query += String.format("\"%s\" < %s AND ", columnName,
                            range.getHighBoundedValue());
                }
            }
        }

        return query;
    }
}
