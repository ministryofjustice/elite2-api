package net.syscon.elite.repository.impl;

import net.syscon.elite.api.support.PageRequest;
import net.syscon.util.QueryBuilderFactory;
import net.syscon.util.SQLProvider;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public abstract class RepositoryBase  {
	@Autowired
	protected NamedParameterJdbcOperations jdbcTemplate;

	@Autowired
	protected SQLProvider sqlProvider;

	@Autowired
	protected QueryBuilderFactory queryBuilderFactory;

	@PostConstruct
	public void initSql() {
		sqlProvider.loadSql(getClass().getSimpleName().replace('.', '/'));
	}

	protected MapSqlParameterSource createParams(Object ... params) {
		return new MapSqlParameterSource(array2map(params));
	}

	protected MapSqlParameterSource createParamSource(PageRequest pageRequest, Object... params) {
		Validate.notNull(pageRequest, "Page request must be provided.");


		MapSqlParameterSource parameterSource = new MapSqlParameterSource();

		parameterSource.addValue("offset", pageRequest.getOffset());
		parameterSource.addValue("limit", pageRequest.getLimit());

		parameterSource.addValues(array2map(params));

		return parameterSource;
	}

	// Converts one-dimensional array of key/value pairs to map
	private Map<String,Object> array2map(Object... params) {
		Validate.isTrue(params.length % 2 == 0, "Additional parameters must be provided as key/value pairs.");

		Map<String,Object> theMap = new HashMap<>();

		for (int i = 0; i < params.length / 2; i++) {
			int j = i * 2;

			theMap.put(params[j].toString(), params[j + 1]);
		}

		return theMap;
	}

	public String getQuery(final String name) {
		return sqlProvider.get(name);
	}
}
