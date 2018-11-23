package net.syscon.util;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.syscon.elite.repository.mapping.FieldMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QueryUtil {
	private QueryUtil() {
	}
	
	public static String getSqlFieldName(final Map<String,FieldMapper> fieldMap, final String jsonField) {
		return fieldMap.entrySet()
										.stream()
										.filter(entry -> entry.getValue().getName().equals(jsonField))
										.map(Map.Entry::getKey)
										.findAny()
										.orElse(null);
	}
	
	public static String getSqlOperator(final String jsonOperator) {
		return Arrays.stream(QueryOperator.values())
									.filter(queryOperator-> queryOperator.getJsonOperator().equalsIgnoreCase(jsonOperator))
									.map(QueryOperator::getSqlOperator)
									.findAny().orElse(null);
	}
	
	public static List<String> checkPrecedencyAndSplit(final String queryInput, final List<String> queryBreak) {
		if(queryInput.contains("(") && queryInput.contains(")")) {
			final String beforePrecedence = queryInput.substring(0,queryInput.indexOf('(')); //It will never have precedence.
			queryBreak.add(beforePrecedence);
			final String precedenceString = queryInput.substring(queryInput.indexOf('(') ,queryInput.indexOf(')')+1);// It has and identified.
			queryBreak.add(precedenceString);
			final String afterPrecedence = queryInput.substring(queryInput.indexOf(')')+1, queryInput.length());// Check it again.
			checkPrecedencyAndSplit(afterPrecedence, queryBreak);
		} else {
			queryBreak.add(queryInput);
		}
		return queryBreak;
	}

	public static String prepareQuery(final String queryItem, final boolean isPrecedence, final Map<String, FieldMapper> fieldMap) {
		final StringBuilder stringBuilder = new StringBuilder();
		final String[] fields = queryItem.split(",");

		Arrays.stream(fields).filter(StringUtils::isNotBlank).forEach(fieldItem -> {
			if (StringUtils.equals(fieldItem, fields[0]) && !StringUtils.startsWithAny(fieldItem, "and:", "or:")) {
				fieldItem = "and:" + fieldItem;
			}

			final String[] operatorFieldValue = fieldItem.split(":");
			final int size = operatorFieldValue.length;
			
			final String connector = size == 3 ? "" : operatorFieldValue[0]; // Get Real SQL Operator From Enum
			final String fieldName = size == 3 ? operatorFieldValue[0] : operatorFieldValue[1];// Get Field Name form Field Mapper
			final String operator = size == 3 ? operatorFieldValue[1] : operatorFieldValue[2];// Get Real SQL Operator From Enum
			String value = size == 3 ? operatorFieldValue[2] : operatorFieldValue[3];// Supply it as it is.
			final String format = size == 5 ? operatorFieldValue[4] : "";
			
			if (value != null && (value.contains("|") || QueryOperator.IN.name().equalsIgnoreCase(getSqlOperator(operator)))) {
				value = "(" + value.replaceAll("\\|", ",") + ")";
			}

			String sqlFieldName = getSqlFieldName(fieldMap, fieldName);
			FieldMapper fieldMapper = fieldMap.get(sqlFieldName);
			String encodedValue = fieldMapper.getEncodedValue(value);

			stringBuilder.append(" ")
				.append(StringUtils.isNotBlank(connector) ? getSqlOperator(connector) : "")
				.append(" ")
				.append(isPrecedence && StringUtils.equalsAny(fieldItem,"and:" + fields[0], fields[0]) ? "(" : "")
				.append(" ")
				.append(sqlFieldName)
				.append(" ")
				.append(getSqlOperator(operator))
				.append(" ")
                .append(applyFormat(encodedValue, format))
				.append(" ")
				.append(isPrecedence && StringUtils.equals(fieldItem, fields[fields.length-1]) ? ")" : "")
				.append(" ");
		});

		return stringBuilder.toString().trim();
	}

	public static String getCriteriaFromQuery(final String sql) {
		try {
            Select select = (Select) CCJSqlParserUtil.parse(sql);
            PlainSelect pl = (PlainSelect) select.getSelectBody();

            StringBuilder criteria = new StringBuilder();
            final FromItem fromItem = pl.getFromItem();
            criteria.append("FROM ").append(fromItem.toString());

            final List<Join> joins = pl.getJoins();
            if (joins != null) {
                criteria.append(" ").append(StringUtils.join(joins, " "));
            }

            final Expression whereExp = pl.getWhere();
            if (whereExp != null) {
                criteria.append(" WHERE ").append(whereExp.toString());
            }
            return criteria.toString();

        } catch (JSQLParserException e) {
		    throw new InvalidDataAccessResourceUsageException(sql, e);
        }
	}

	/**
	 * Applies SQL TO_DATE conversion to specified date string using ISO-8601 date pattern (YYYY-MM-DD).
	 *
	 * @param dateValue ISO-8601 string representation of date - e.g. '2017-04-23'.
	 * @return SQL conversion of date string to date type - e.g. {@code TO_DATE('2017-04-23', 'YYYY-MM-DD')}.
	 */
	public static String convertToDate(String dateValue) {
		return convertToDate(dateValue, DateTimeConverter.ISO_LOCAL_DATE_FORMAT_PATTERN);
	}

	// Assumes use for date formatting only - with proposed deprecation of API that uses generic query format, a more
	// sophisticated implementation is not worthwhile.
	private static String applyFormat(String value, String format) {
		// If value already 'converted' to date or there is no format specified, just return value, otherwise apply format.
		return (value.startsWith("TO_DATE") || StringUtils.isBlank(format)) ? value : convertToDate(value, format);
	}

	private static String convertToDate(String dateValue, String dateFormat) {
		if (StringUtils.isBlank(dateValue)) {
			throw new IllegalArgumentException("dateValue must be provided");
		}

		if (StringUtils.isBlank(dateFormat)) {
			throw new IllegalArgumentException("dateFormat must be provided");
		}

		String quotedDateValue = StringUtils.appendIfMissing(StringUtils.prependIfMissing(dateValue, "'"), "'");
		String quotedDateFormat = StringUtils.appendIfMissing(StringUtils.prependIfMissing(dateFormat, "'"), "'");

		return "TO_DATE("+ quotedDateValue + ", " + quotedDateFormat + ")";
	}
}
