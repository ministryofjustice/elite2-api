package net.syscon.util;

import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.mapping.FieldMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.syscon.util.QueryUtil.prepareQuery;

public abstract class AbstractQueryBuilder implements IQueryBuilder {

    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    protected final String initialSQL;
    protected final Map<String, FieldMapper> fieldMap;
    protected final Map<String, String> fieldNameToColumnMap;

    protected boolean includePagination;
    protected boolean includeRowCount;
    protected boolean includeDirectRowCount;

    protected final StringBuilder extraWhere = new StringBuilder();

    protected String extraOrderBy = "";
    protected DatabaseDialect dialect;
    protected boolean removeSpecialChars;

    protected AbstractQueryBuilder(String initialSQL, Map<String, FieldMapper> fieldMap, DatabaseDialect dialect) {
        this.initialSQL = initialSQL;
        this.dialect = dialect;
        this.fieldMap = fieldMap;
        if (fieldMap != null) {
            this.fieldNameToColumnMap = fieldMap.entrySet().stream()
                    .collect(Collectors.toMap(v -> v.getValue().getName(),
                            Map.Entry::getKey));
        } else {
            this.fieldNameToColumnMap = Collections.emptyMap();
        }
    }

    @Override
    public DatabaseDialect getDialect() {
        return this.dialect;
    }

    @Override
    public IQueryBuilder removeSpecialChars() {
        this.removeSpecialChars = true;
        return this;
    }

    @Override
    public IQueryBuilder addPagination() {
        includePagination = true;

        return this;
    }

    @Override
    public IQueryBuilder addRowCount() {
        includeRowCount = true;

        return this;
    }

    @Override
    public IQueryBuilder addDirectRowCount() {
        includeDirectRowCount = true;

        return this;
    }

    @Override
    public IQueryBuilder addQuery(final String query) {
        if (StringUtils.isNotBlank(query)) {
            List<String> queryTerms = new ArrayList<>();

            List<String> queryList = QueryUtil.checkPrecedencyAndSplit(query, new ArrayList<>());

            queryList.stream()
                    .filter(StringUtils::isNotBlank)
                    .forEach(queryItem -> {
                        if (queryItem.contains("(") && queryItem.contains(")")) {
                            String modifiedQueryItem = queryItem
                                    .replace("(", "")
                                    .replace(")", "");

                            queryTerms.add(prepareQuery(modifiedQueryItem, true, fieldMap));
                        } else {
                            queryTerms.add(prepareQuery(queryItem, false, fieldMap));
                        }
                    });

            String fullQuery = StringUtils.normalizeSpace(StringUtils.join(queryTerms, " "));

            if (fullQuery.startsWith("AND ")) {
                extraWhere.append(fullQuery.substring(4));
		    } else if (fullQuery.startsWith("OR ")) {
                extraWhere.append(fullQuery.substring(3));
		    }
        }

        return this;
    }

    @Override
    public IQueryBuilder addWhereClause(String whereClause) {
        if (StringUtils.isNotBlank(whereClause)) {

            if (whereClause.startsWith("AND ")) {
                extraWhere.append(whereClause.substring(4));
            } else if (whereClause.startsWith("OR ")) {
                extraWhere.append(whereClause.substring(3));
            } else {
                extraWhere.append(whereClause);
            }
        }

        return this;
    }

    @Override
    public IQueryBuilder addOrderBy(boolean isAscending, String fields) {

        if (extraOrderBy.length() > 0) {
            extraOrderBy += ", ";
        }
        extraOrderBy += COMMA_PATTERN
                .splitAsStream(fields == null ? "" : fields)
                .map(fieldNameToColumnMap::get)
                .filter(Objects::nonNull)
                .map(s -> s + " " + addOrderDirection(isAscending))
                .collect(Collectors.joining(", "));

        return this;
    }

    @Override
    public IQueryBuilder addOrderBy(Order order, String fields) {
        return addOrderBy(Order.ASC == order, fields);
    }

    @Override
    public IQueryBuilder addOrderBy(PageRequest pageRequest) {
        return addOrderBy(pageRequest.isAscendingOrder(), pageRequest.getOrderBy());
    }

    private SQLKeyword addOrderDirection(boolean isAscending) {
        return isAscending ? SQLKeyword.ASC : SQLKeyword.DESC;
    }

    protected String removeSpecialCharacters(final String sql) {
        if (sql == null) return null;
        final String stmts[] = { sql, sql.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ') };
        while (!stmts[0].equals(stmts[1])) {
            stmts[0] = stmts[1];
            stmts[1] = stmts[1].replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
            stmts[1] = stmts[1].replaceAll("  ", " ");
        }
        return stmts[0].trim();
    }

    protected Optional<SQLKeyword> getStatementType() {
        String modifiedSQL = initialSQL.toUpperCase()
                .replace('\t', ' ')
                .replace('\r', ' ');

        String[] lines = modifiedSQL.split("\\n");

        SQLKeyword statementType = null;

        for (String line : lines) {
            if (StringUtils.startsWithIgnoreCase(line.trim(), SQLKeyword.SELECT.toString())) {
                statementType = SQLKeyword.SELECT;

                break;
            }

            if (StringUtils.startsWithIgnoreCase(line.trim(), SQLKeyword.INSERT.toString())) {
                statementType = SQLKeyword.INSERT;

                break;
            }

            if (StringUtils.startsWithIgnoreCase(line.trim(), SQLKeyword.UPDATE.toString())) {
                statementType = SQLKeyword.UPDATE;

                break;
            }

            if (StringUtils.startsWithIgnoreCase(line.trim(), SQLKeyword.DELETE.toString())) {
                statementType = SQLKeyword.DELETE;

                break;
            }
        }

        return Optional.ofNullable(statementType);
    }

    @Override
    public abstract String build();
}
