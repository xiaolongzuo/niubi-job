/*
 * Copyright 2002-2016 the original author or authors.
 *
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
 */

package com.zuoxiaolong.niubi.job.persistent.hibernate;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.StringTokenizer;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Component
public class HibernateNamingStrategy extends PhysicalNamingStrategyStandardImpl {

	private static final long serialVersionUID = -6608653199171747403L;

	private static final String SEPARATOR = "_";

	private int maxLength = 30;

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

    public HibernateNamingStrategy() {
        super();
    }

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment context) {
        return addUnderscores(name);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment context) {
        return addUnderscores(name);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return addUnderscores(name);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
        return addUnderscores(name);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return addUnderscores(name);
    }

    private Identifier addUnderscores(Identifier name) {
        if (name != null) {
            return Identifier.toIdentifier(addUnderscores(abbreviateName(name.getText(), maxLength)));
        } else {
            return name;
        }
    }

    private static String addUnderscores(String name) {
        StringBuilder buf = new StringBuilder( name.replace('.', '_') );
        for (int i=1; i<buf.length()-1; i++) {
            if (
                    Character.isLowerCase( buf.charAt(i-1) ) &&
                            Character.isUpperCase( buf.charAt(i) ) &&
                            Character.isLowerCase( buf.charAt(i+1) )
                    ) {
                buf.insert(i++, '_');
            }
        }
        return buf.toString().toLowerCase(Locale.ROOT);
    }

    private static String abbreviateName(String someName, int maxLength) {
		if (someName.length() <= maxLength)
			return someName;

		String[] tokens = splitName(someName);
		shortenName(someName, tokens, maxLength);

		return assembleResults(tokens);
	}

	private static String[] splitName(String someName) {
		StringTokenizer tokenizer = new StringTokenizer(someName, SEPARATOR);
		String[] tokens = new String[tokenizer.countTokens()];
		int i = 0;
		while (tokenizer.hasMoreTokens()) {
			tokens[i] = tokenizer.nextToken();
			i++;
		}
		return tokens;
	}

	private static void shortenName(String someName, String[] tokens, int maxLength) {
		int currentLength = someName.length();
		while (currentLength > maxLength) {
			if (isAllOneLength(tokens)) {
				String oldToken = tokens[tokens.length - 1];
				String[] newTokens = new String[tokens.length - 1];
				System.arraycopy(tokens, 0, newTokens, 0, newTokens.length);
				tokens = newTokens;
				currentLength -= oldToken.length() + 1;
			} else {
				int tokenIndex = getIndexOfLongest(tokens);
				String oldToken = tokens[tokenIndex];
				tokens[tokenIndex] = oldToken.substring(0, (oldToken.length() - 1 >= 0) ? (oldToken.length() - 1) : 0);
				currentLength -= oldToken.length() - tokens[tokenIndex].length();
			}
		}
	}

	private static boolean isAllOneLength(String[] tokens) {
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].length() > 1) {
				return false;
			}
		}
		return true;
	}

	private static String assembleResults(String[] tokens) {
		StringBuilder result = new StringBuilder(tokens[0]);
		for (int j = 1; j < tokens.length; j++) {
			if (tokens[j].trim().length() == 0) {
				continue;
			}
			result.append(SEPARATOR).append(tokens[j]);
		}
		return result.toString();
	}

	private static int getIndexOfLongest(String[] tokens) {
		int maxLength = 0;
		int index = -1;
		for (int i = 0; i < tokens.length; i++) {
			String string = tokens[i];
			if (maxLength < string.length()) {
				maxLength = string.length();
				index = i;
			}
		}
		return index;
	}

}
