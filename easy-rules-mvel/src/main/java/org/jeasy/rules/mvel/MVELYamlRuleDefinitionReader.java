/**
 * The MIT License
 *
 *  Copyright (c) 2019, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package org.jeasy.rules.mvel;

import org.jeasy.rules.api.Rule;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rule definition reader based on <a href="https://github.com/FasterXML/jackson-dataformats-text/tree/master/yaml">Jackson Yaml</a>.
 *
 * This reader expects a collection of rule definitions as input even for a single rule. For example:
 *
 * <pre>
 *     rule1
 *     ---
 *     rule2
 * </pre>
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
@SuppressWarnings("unchecked")
public class MVELYamlRuleDefinitionReader implements MVELRuleDefinitionReader {

    private Yaml yaml;

    /**
     * Create a new {@link MVELYamlRuleDefinitionReader}.
     */
    public MVELYamlRuleDefinitionReader() {
        yaml = new Yaml();
    }

    /**
     * Create a new {@link MVELYamlRuleDefinitionReader}.
     *
     * @param yaml to use to read rule definitions
     */
    public MVELYamlRuleDefinitionReader(Yaml yaml) {
        this.yaml = yaml;
    }

    public List<MVELRuleDefinition> read(Reader reader) {
        List<MVELRuleDefinition> ruleDefinitions = new ArrayList<>();
        Iterable<Object> rules = yaml.loadAll(reader);
        for (Object rule : rules) {
            Map<String, Object> map = (Map<String, Object>) rule;
            ruleDefinitions.add(createRuleDefinitionFrom(map));
        }
        return ruleDefinitions;
    }

    private MVELRuleDefinition createRuleDefinitionFrom(Map<String, Object> map) {
        MVELRuleDefinition ruleDefinition = new MVELRuleDefinition();

        String name = (String) map.get("name");
        ruleDefinition.setName(name != null ? name : Rule.DEFAULT_NAME);

        String description = (String) map.get("description");
        ruleDefinition.setDescription(description != null ? description : Rule.DEFAULT_DESCRIPTION);

        Integer priority = (Integer) map.get("priority");
        ruleDefinition.setPriority(priority != null ? priority : Rule.DEFAULT_PRIORITY);

        String compositeRuleType = (String) map.get("compositeRuleType");

        String condition = (String) map.get("condition");
        if (condition == null && compositeRuleType == null) {
            throw new IllegalArgumentException("The rule condition must be specified");
        }
        ruleDefinition.setCondition(condition);

        List<String> actions = (List<String>) map.get("actions");
        if ((actions == null || actions.isEmpty()) && compositeRuleType == null) {
            throw new IllegalArgumentException("The rule action(s) must be specified");
        }
        ruleDefinition.setActions(actions);

        List<Object> composingRules = (List<Object>) map.get("composingRules");
        if (composingRules != null && compositeRuleType == null) {
            throw new IllegalArgumentException("Non-composite rules cannot have composing rules");
        } else if (composingRules == null && compositeRuleType != null) {
            throw new IllegalArgumentException("Composite rules must have composing rules specified");
        } else if (composingRules != null) {
            List<MVELRuleDefinition> composingRuleDefinitions = new ArrayList<>();
            for (Object rule : composingRules){
                Map<String, Object> composingRuleMap = (Map<String, Object>) rule;
                composingRuleDefinitions.add(createRuleDefinitionFrom(composingRuleMap));
            }
            ruleDefinition.setComposingRules(composingRuleDefinitions);
            ruleDefinition.setCompositeRuleType(compositeRuleType);
        }

        return ruleDefinition;
    }
}
