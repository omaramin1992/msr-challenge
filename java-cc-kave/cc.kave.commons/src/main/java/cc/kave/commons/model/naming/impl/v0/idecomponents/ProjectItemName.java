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

import cc.kave.commons.exceptions.ValidationException;
import cc.kave.commons.model.naming.idecomponents.IProjectItemName;
import cc.kave.commons.model.naming.impl.v0.BaseName;

public class ProjectItemName extends BaseName implements IProjectItemName {

	public ProjectItemName() {
		this("?");
	}

	public ProjectItemName(String identifier) {
		super(identifier);
		if (!isUnknown() && !identifier.contains(" ")) {
			throw new ValidationException("must contain a space");
		}
	}

	private String[] _parts;

	private String[] getParts() {
		if (_parts == null) {
			_parts = identifier.split(" ", 2);
		}
		return _parts;
	}

	@Override
	public String getType() {
		return isUnknown() ? UNKNOWN_NAME_IDENTIFIER : getParts()[0];
	}

	@Override
	public String getName() {
		return isUnknown() ? UNKNOWN_NAME_IDENTIFIER : getParts()[1];
	}

	@Override
	public boolean isUnknown() {
		return "?".equals(identifier);
	}
}