/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package cc.kave.commons.model.naming.impl.v0.idecomponents;

import static cc.kave.commons.utils.StringUtils.f;

import cc.kave.commons.exceptions.ValidationException;
import cc.kave.commons.model.naming.idecomponents.IDocumentName;
import cc.kave.commons.model.naming.impl.v0.BaseName;

public class DocumentName extends BaseName implements IDocumentName {

	public DocumentName() {
		this(UNKNOWN_NAME_IDENTIFIER);
	}

	public DocumentName(String identifier) {
		super(identifier);
		if (!UNKNOWN_NAME_IDENTIFIER.equals(identifier)) {
			if (!identifier.contains(" ")) {
				throw new ValidationException(f("document name is missing a space '%s'", identifier));
			}
		}
	}

	private String[] _parts;

	private String[] getParts() {
		if (_parts == null) {
			if (identifier.startsWith("Plain Text ")) {
				_parts = new String[] { "Plain Text", identifier.substring(11) };
			} else {
				_parts = identifier.split(" ", 2);
			}
		}
		return _parts;
	}

	@Override
	public String getLanguage() {
		return isUnknown() ? UNKNOWN_NAME_IDENTIFIER : getParts()[0];
	}

	@Override
	public String getFileName() {
		return isUnknown() ? UNKNOWN_NAME_IDENTIFIER : getParts()[1];
	}

	@Override
	public boolean isUnknown() {
		return UNKNOWN_NAME_IDENTIFIER.equals(identifier);
	}
}