package org.hcjf.layers.query.compilers;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.Layer;
import org.hcjf.layers.query.Join;
import org.hcjf.layers.query.Query;
import org.hcjf.layers.query.evaluators.*;
import org.hcjf.layers.query.model.*;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Strings;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SQLCompiler extends Layer implements QueryCompiler {

    private static final String NAME = "SQL";

    @Override
    public String getImplName() {
        return NAME;
    }

    /**
     * Create a query instance from a expression.
     * @param queryExpression Expression that represents a query.
     * @return Query instance.
     */
    @Override
    public Query compile(String queryExpression) {
        Query result = null;
        List<String> richTexts = Strings.groupRichText(queryExpression);
        List<String> groups = Strings.replaceableGroup(Strings.removeLines(richTexts.get(richTexts.size() - 1)));
        result = compile(groups, richTexts, groups.size() - 1, new AtomicInteger(0));
        return result;
    }

    /**
     * Creates a query with next structure 'SELECT * FROM {resourceName}'
     * @param resourceName Resource name.
     * @return Returns a single query instance.
     */
    public Query compileSingleQuery(String resourceName) {
        return compile(String.format(SystemProperties.get(SystemProperties.Query.SINGLE_PATTERN), resourceName));
    }

    /**
     * Create a query instance from sql definition.
     * @param groups
     * @param startGroup
     * @return Query instance.
     */
    private Query compile(List<String> groups, List<String> richTexts, Integer startGroup, AtomicInteger placesIndex) {
        Query query;

        String[] unions = groups.get(startGroup).split(SystemProperties.get(SystemProperties.Query.UNION_REGULAR_EXPRESSION));
        String queryDefinition = unions[0].trim();
        Pattern pattern = SystemProperties.getPattern(SystemProperties.Query.SELECT_REGULAR_EXPRESSION);
        Matcher matcher = pattern.matcher(queryDefinition);

        if(matcher.matches()) {
            String selectBody = matcher.group(SystemProperties.get(SystemProperties.Query.SELECT_GROUP_INDEX));
            selectBody = selectBody.replaceFirst(Strings.CASE_INSENSITIVE_REGEX_FLAG+SystemProperties.get(SystemProperties.Query.ReservedWord.SELECT), Strings.EMPTY_STRING);
            String fromBody = matcher.group(SystemProperties.get(SystemProperties.Query.FROM_GROUP_INDEX));
            fromBody = fromBody.replaceFirst(Strings.CASE_INSENSITIVE_REGEX_FLAG+SystemProperties.get(SystemProperties.Query.ReservedWord.FROM), Strings.EMPTY_STRING);
            String conditionalBody = matcher.group(SystemProperties.get(SystemProperties.Query.CONDITIONAL_GROUP_INDEX));
            if(conditionalBody != null && conditionalBody.endsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.STATEMENT_END))) {
                conditionalBody = conditionalBody.substring(0, conditionalBody.indexOf(SystemProperties.get(SystemProperties.Query.ReservedWord.STATEMENT_END))-1);
            }

            String resourceValue = matcher.group(SystemProperties.get(SystemProperties.Query.RESOURCE_VALUE_INDEX));
            String dynamicResource = matcher.group(SystemProperties.get(SystemProperties.Query.DYNAMIC_RESOURCE_INDEX));
            String dynamicResourceAlias = matcher.group(SystemProperties.get(SystemProperties.Query.DYNAMIC_RESOURCE_ALIAS_INDEX));
            query = new Query(createResource(resourceValue, dynamicResource, dynamicResourceAlias, groups, richTexts));

            if(conditionalBody != null) {
                Pattern conditionalPatter = SystemProperties.getPattern(SystemProperties.Query.CONDITIONAL_REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
                List<String> conditionalElements = List.of(conditionalPatter.split(conditionalBody)).stream().filter(S -> !S.isBlank()).collect(Collectors.toList());
                String element;
                String elementValue;
                for (int i = 0; i < conditionalElements.size(); i++) {
                    element = conditionalElements.get(i++).trim();
                    elementValue = conditionalElements.get(i).trim();
                    if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.JOIN)) ||
                            element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.FULL)) ||
                            element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.INNER)) ||
                            element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LEFT)) ||
                            element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.RIGHT))) {

                        Join.JoinType type = Join.JoinType.valueOf(element.toUpperCase());
                        if(type != Join.JoinType.JOIN) {
                            elementValue = conditionalElements.get(++i).trim();
                        }

                        String joinConditionalBody;
                        QueryResource joinResource;
                        Pattern joinPattern = SystemProperties.getPattern(SystemProperties.Query.JOIN_REGULAR_EXPRESSION);
                        Matcher joinMatcher = joinPattern.matcher(elementValue);
                        if(joinMatcher.matches()) {
                            String joinDynamicResource = joinMatcher.group(SystemProperties.get(SystemProperties.Query.JOIN_DYNAMIC_RESOURCE_INDEX));
                            String joinResourceValue = joinMatcher.group(SystemProperties.get(SystemProperties.Query.JOIN_RESOURCE_VALUE_INDEX));
                            String joinDynamicResourceAlias = joinMatcher.group(SystemProperties.get(SystemProperties.Query.JOIN_DYNAMIC_RESOURCE_ALIAS_INDEX));
                            joinResource = createResource(joinResourceValue, joinDynamicResource, joinDynamicResourceAlias, groups, richTexts);
                            joinConditionalBody = joinMatcher.group(SystemProperties.get(SystemProperties.Query.JOIN_CONDITIONAL_BODY_INDEX));
                            joinConditionalBody = Strings.reverseGrouping(joinConditionalBody, groups);
                            joinConditionalBody = Strings.reverseRichTextGrouping(joinConditionalBody, richTexts);
                        } else {
                            throw new HCJFRuntimeException("Join syntax wrong, near '%s'", elementValue);
                        }

                        Join join = new Join(query, joinResource, type);
                        query.getResources().add(join.getResource());
                        completeEvaluatorCollection(query, joinConditionalBody, groups, richTexts, join, 0, placesIndex);
                        query.addJoin(join);
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.WHERE))) {
                        completeEvaluatorCollection(query, elementValue, groups, richTexts, query, 0, placesIndex);
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.ORDER_BY))) {
                        for (String orderField : elementValue.split(SystemProperties.get(
                                SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                            query.addOrderParameter((QueryOrderParameter)
                                    processStringValue(query, groups, richTexts, orderField, placesIndex, QueryOrderParameter.class, new ArrayList<>()));
                        }
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GROUP_BY))) {
                        for (String orderField : elementValue.split(SystemProperties.get(
                                SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                            query.addGroupField((QueryReturnParameter)
                                    processStringValue(query, groups, richTexts, orderField, placesIndex, QueryReturnParameter.class, new ArrayList<>()));
                        }
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LIMIT))) {
                        if(elementValue == null || elementValue.isBlank()) {
                            throw new HCJFRuntimeException("Undeclared limit value");
                        }

                        String[] limitValues = elementValue.split(Strings.ARGUMENT_SEPARATOR);
                        if(limitValues.length > 0 && !limitValues[0].isBlank()) {
                            try {
                                query.setLimit(Integer.parseInt(limitValues[0].trim()));
                            } catch (NumberFormatException ex) {
                                throw new HCJFRuntimeException("The limit value must be an integer", ex);
                            }
                        }

                        if(limitValues.length > 1 && !limitValues[1].isBlank()) {
                            try {
                                query.setUnderlyingLimit(Integer.parseInt(limitValues[1].trim()));
                            } catch (NumberFormatException ex) {
                                throw new HCJFRuntimeException("The underlying limit value must be an integer", ex);
                            }
                        }
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.START))) {
                        if(elementValue == null || elementValue.isBlank()) {
                            throw new HCJFRuntimeException("Undeclared start value");
                        }

                        String[] startValues = elementValue.split(Strings.ARGUMENT_SEPARATOR);
                        if(startValues.length > 0 && !startValues[0].isBlank()) {
                            try {
                                query.setStart(Integer.parseInt(startValues[0].trim()));
                            } catch (NumberFormatException ex) {
                                throw new HCJFRuntimeException("The start value must be an integer", ex);
                            }
                        }

                        if(startValues.length > 1 && !startValues[1].isBlank()) {
                            try {
                                query.setUnderlyingStart(Integer.parseInt(startValues[1].trim()));
                            } catch (NumberFormatException ex) {
                                throw new HCJFRuntimeException("The underlying start value must be an integer", ex);
                            }
                        }
                    }
                }
            }

            for(String returnField : selectBody.split(SystemProperties.get(
                    SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                query.addReturnField((QueryReturnParameter)
                        processStringValue(query, groups, richTexts, returnField, placesIndex, QueryReturnParameter.class, new ArrayList<>()));
            }
        } else {
            String value = queryDefinition;
            int place = Strings.getNoMatchPlace(matcher, queryDefinition);
            String nearFrom = Strings.getNearFrom(value, place, 5);
            throw new HCJFRuntimeException("Query match fail near from ( '...%s...' ), query body: '%s'", nearFrom, value);
        }

        for (int i = 2; i < unions.length; i+=2) {
            groups.set(groups.size() - 1, unions[i].trim());
            query.addUnion(compile(groups, richTexts, startGroup, placesIndex));
        }

        return query;
    }

    /**
     * Creates the resource implementation depends of the values.
     * @param resourceValue Resource value definition.
     * @param dynamicResource Dynamic resource value.
     * @param dynamicResourceAlias Dynamic resource alias.
     * @param groups Groups collection.
     * @param richTexts Rich texts collection.
     * @return Returns the resource implementation.
     */
    private QueryResource createResource(String resourceValue, String dynamicResource, String dynamicResourceAlias, List<String> groups, List<String> richTexts) {
        QueryResource result;
        if(dynamicResource.isBlank()) {
            result = new QueryResource(resourceValue.trim());
        } else {
            String path = null;
            if (resourceValue.indexOf(Strings.CLASS_SEPARATOR) > 0) {
                path = resourceValue.substring(resourceValue.indexOf(Strings.CLASS_SEPARATOR) + 1).trim();
                resourceValue = resourceValue.substring(0, resourceValue.indexOf(Strings.CLASS_SEPARATOR));
            }
            resourceValue = Strings.reverseGrouping(resourceValue, groups);
            resourceValue = Strings.reverseRichTextGrouping(resourceValue, richTexts);
            resourceValue = resourceValue.substring(1, resourceValue.length() - 1);
            Query subQuery;
            if (resourceValue.toUpperCase().startsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.SELECT))) {
                subQuery = compile(resourceValue);
            } else {
                subQuery = compileSingleQuery(resourceValue);
            }
            result = new QueryDynamicResource(dynamicResourceAlias.trim(), subQuery, path);
        }
        return result;
    }

    /**
     * Complete the evaluator collections with all the evaluator definitions in the groups.
     * @param groups Where groups.
     * @param parentCollection Parent collection.
     * @param definitionIndex Definition index into the groups.
     */
    private final void completeEvaluatorCollection(Query query, String startElement, List<String> groups, List<String> richTexts,
                                                          EvaluatorCollection parentCollection,
                                                          Integer definitionIndex,
                                                          AtomicInteger placesIndex) {
        Pattern wherePatter = SystemProperties.getPattern(SystemProperties.Query.EVALUATOR_COLLECTION_REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
        String[] evaluatorDefinitions;
        if(startElement != null) {
            evaluatorDefinitions = wherePatter.split(startElement);
        } else {
            evaluatorDefinitions = wherePatter.split(groups.get(definitionIndex));
        }
        EvaluatorCollection collection = null;
        List<String> pendingDefinitions = new ArrayList<>();
        for(String definition : evaluatorDefinitions) {
            definition = definition.trim();
            if (definition.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.AND))) {
                if (collection == null) {
                    if(parentCollection instanceof Query || parentCollection instanceof Join || parentCollection instanceof And) {
                        collection = parentCollection;
                    } else {
                        collection = parentCollection.and();
                    }
                } else if (collection instanceof Or) {
                    if(parentCollection instanceof Query || parentCollection instanceof Join || parentCollection instanceof And) {
                        collection = parentCollection;
                    } else {
                        collection = parentCollection.and();
                    }
                }
            } else if (definition.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.OR))) {
                if (collection == null) {
                    if(parentCollection instanceof Or) {
                        collection = parentCollection;
                    } else {
                        collection = parentCollection.or();
                    }
                } else if(collection instanceof Query || collection instanceof Join || collection instanceof And) {
                    if(parentCollection instanceof Or) {
                        collection = parentCollection;
                    } else {
                        collection = parentCollection.or();
                    }
                }
            } else {
                pendingDefinitions.add(definition);
                if(collection != null) {
                    for(String pendingDefinition : pendingDefinitions) {
                        processDefinition(query, pendingDefinition, collection, groups, richTexts, placesIndex);
                    }
                    pendingDefinitions.clear();
                } else if(pendingDefinitions.size() > 1) {
                    throw new IllegalArgumentException("");
                }
            }
        }

        for(String pendingDefinition : pendingDefinitions) {
            if(collection != null) {
                processDefinition(query, pendingDefinition, collection, groups, richTexts, placesIndex);
            } else {
                processDefinition(query, pendingDefinition, parentCollection, groups, richTexts, placesIndex);
            }
        }
    }

    /**
     * Creates a conditional evaluator from string representation.
     * @param definition String definition of the conditional.
     * @param collection Evaluator collection to put the conditional processed.
     * @param groups Sub representation of the main representation.
     * @param placesIndex Place counter of the group list.
     */
    private void processDefinition(Query query, String definition, EvaluatorCollection collection, List<String> groups, List<String> richTexts, AtomicInteger placesIndex) {
        String[] evaluatorValues;
        Object leftValue;
        Object rightValue;
        String firstArgument;
        String secondArgument;
        String operator;
        Evaluator evaluator;

        evaluatorValues = definition.split(SystemProperties.get(SystemProperties.Query.OPERATION_REGULAR_EXPRESSION));
        if (evaluatorValues.length == 1 && definition.startsWith(Strings.REPLACEABLE_GROUP)) {
            Integer index = Integer.parseInt(definition.replace(Strings.REPLACEABLE_GROUP, Strings.EMPTY_STRING));
            completeEvaluatorCollection(query, null, groups, richTexts, collection, index, placesIndex);
        } else {
            boolean operatorDone = false;
            firstArgument = Strings.EMPTY_STRING;
            secondArgument = Strings.EMPTY_STRING;
            operator = Strings.EMPTY_STRING;
            for (String evaluatorValue : evaluatorValues) {
                evaluatorValue = evaluatorValue.trim();
                if (evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.NOT))) {
                    operator += evaluatorValue + Strings.WHITE_SPACE;
                } else if (evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.NOT_2))) {
                    operator += evaluatorValue;
                } else if (evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DISTINCT))
                        || evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DISTINCT_2))
                        || evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.EQUALS))
                        || evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN))
                        || evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN_OR_EQUALS))
                        || evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.IN))
                        || evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LIKE))
                        || evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN))
                        || evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN_OR_EQUALS))) {
                    operator += evaluatorValue;
                    operatorDone = true;
                } else if (operatorDone) {
                    secondArgument += evaluatorValue + Strings.WHITE_SPACE;
                } else {
                    firstArgument += evaluatorValue + Strings.WHITE_SPACE;
                }
            }

            List<QueryField> presentFields = new ArrayList<>();
            if (operator == null || operator.trim().isEmpty()) {
                leftValue = processStringValue(query, groups, richTexts, firstArgument.trim(), placesIndex, QueryParameter.class, presentFields);
                evaluator = new BooleanEvaluator(leftValue);
            } else {
                leftValue = processStringValue(query, groups, richTexts, firstArgument.trim(), placesIndex, QueryParameter.class, presentFields);
                if (leftValue instanceof String) {
                    leftValue = Strings.reverseGrouping((String) leftValue, groups);
                }
                rightValue = processStringValue(query, groups, richTexts, secondArgument.trim(), placesIndex, QueryParameter.class, presentFields);
                if (rightValue instanceof String) {
                    rightValue = Strings.reverseGrouping((String) rightValue, groups);
                }
                operator = operator.trim();


                if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DISTINCT)) ||
                        operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DISTINCT_2))) {
                    evaluator = new Distinct(leftValue, rightValue);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.EQUALS))) {
                    evaluator = new Equals(leftValue, rightValue);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN))) {
                    evaluator = new GreaterThan(leftValue, rightValue);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN_OR_EQUALS))) {
                    evaluator = new GreaterThanOrEqual(leftValue, rightValue);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.IN))) {
                    evaluator = new In(leftValue, rightValue);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LIKE))) {
                    evaluator = new Like(leftValue, rightValue);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.NOT_IN))) {
                    evaluator = new NotIn(leftValue, rightValue);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN))) {
                    evaluator = new SmallerThan(leftValue, rightValue);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN_OR_EQUALS))) {
                    evaluator = new SmallerThanOrEqual(leftValue, rightValue);
                } else {
                    throw new HCJFRuntimeException("Unsupported operator '%s', near '%s'", operator, definition);
                }
            }

            if(evaluator instanceof BaseEvaluator) {
                ((BaseEvaluator)evaluator).setEvaluatorFields(presentFields);
            }

            collection.addEvaluator(evaluator);
        }
    }

    /**
     * Process the string representation to obtain the specific object type.
     * @param groups Sub representation of the main representation.
     * @param stringValue String representation to process.
     * @param placesIndex Place counter of the group list.
     * @param parameterClass Parameter class.
     * @return Return the specific implementation of the string representation.
     */
    private Object processStringValue(Query query, List<String> groups, List<String> richTexts, String stringValue, AtomicInteger placesIndex, Class parameterClass, List<QueryField> presentFields) {
        Object result = null;
        String trimmedStringValue = stringValue.trim();
        if(trimmedStringValue.equals(SystemProperties.get(SystemProperties.Query.ReservedWord.REPLACEABLE_VALUE))) {
            //If the string value is equals than "?" then the value object is an instance of ReplaceableValue.
            result = new FieldEvaluator.ReplaceableValue(placesIndex.getAndAdd(1));
        } else if(trimmedStringValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.NULL))) {
            result = null;
        } else if(trimmedStringValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.TRUE))) {
            result = true;
        } else if(trimmedStringValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.FALSE))) {
            result = false;
        } else if(trimmedStringValue.startsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.STRING_DELIMITER))) {
            if (trimmedStringValue.endsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.STRING_DELIMITER))) {
                //If the string value start and end with "'" then the value can be a string or a date object.
                trimmedStringValue = trimmedStringValue.substring(1, trimmedStringValue.length() - 1);
                trimmedStringValue = richTexts.get(Integer.parseInt(trimmedStringValue.replace(Strings.REPLACEABLE_RICH_TEXT, Strings.EMPTY_STRING)));

                //Clean the value to remove all the skip characters into the string value.
                trimmedStringValue = trimmedStringValue.replace(Strings.RICH_TEXT_SKIP_CHARACTER + Strings.RICH_TEXT_SEPARATOR, Strings.RICH_TEXT_SEPARATOR);

                try {
                    result = SystemProperties.getDateFormat(SystemProperties.Query.DATE_FORMAT).parse(trimmedStringValue);
                } catch (Exception ex) {
                    //The value is not a date then the value is a string
                    while(trimmedStringValue.contains(Strings.REPLACEABLE_GROUP)) {
                        trimmedStringValue = Strings.reverseGrouping(trimmedStringValue, groups);
                    }
                    result = trimmedStringValue;
                }
            } else {
                throw new HCJFRuntimeException("Expecting string en delimiter, near %s", trimmedStringValue);
            }
        } else if(trimmedStringValue.matches(Strings.REPLACEABLE_EXPRESSION_REGEX)) {
            Integer index = Integer.parseInt(trimmedStringValue.replace(Strings.REPLACEABLE_GROUP, Strings.EMPTY_STRING));
            String group = groups.get(index);
            if(group.toUpperCase().startsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.SELECT))) {
                result = new FieldEvaluator.QueryValue(compile(groups, richTexts, index, placesIndex));
            } else if(!group.matches(SystemProperties.get(SystemProperties.HCJF_UUID_REGEX)) &&
                    group.matches(SystemProperties.get(SystemProperties.HCJF_MATH_CONNECTOR_REGULAR_EXPRESSION)) &&
                    group.matches(SystemProperties.get(SystemProperties.HCJF_MATH_REGULAR_EXPRESSION))) {
                result = processStringValue(query, groups, richTexts, group, placesIndex, parameterClass, presentFields);
            } else {
                //If the string value start with "(" and end with ")" then the value is a collection.
                Collection<Object> collection = new ArrayList<>();
                for (String subStringValue : group.split(SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                    collection.add(processStringValue(query, groups, richTexts, subStringValue.trim(), placesIndex, parameterClass, presentFields));
                }
                result = collection;
            }
        } else if(trimmedStringValue.matches(SystemProperties.get(SystemProperties.HCJF_UUID_REGEX))) {
            result = UUID.fromString(trimmedStringValue);
        } else if(trimmedStringValue.matches(SystemProperties.get(SystemProperties.HCJF_INTEGER_NUMBER_REGEX))) {
            try {
                result = Long.parseLong(trimmedStringValue);
            } catch (Exception ex) {
                result = trimmedStringValue;
            }
        } else if(trimmedStringValue.matches(SystemProperties.get(SystemProperties.HCJF_DECIMAL_NUMBER_REGEX))) {
            try {
                result = SystemProperties.getDecimalFormat(SystemProperties.Query.DECIMAL_FORMAT).parse(trimmedStringValue);
            } catch (ParseException e) {
                result = trimmedStringValue;
            }
        } else if(trimmedStringValue.matches(SystemProperties.get(SystemProperties.HCJF_SCIENTIFIC_NUMBER_REGEX))) {
            try {
                result = SystemProperties.getDecimalFormat(SystemProperties.Query.SCIENTIFIC_NOTATION_FORMAT).parse(trimmedStringValue);
            } catch (ParseException e) {
                result = trimmedStringValue;
            }
        } else if(trimmedStringValue.matches(SystemProperties.get(SystemProperties.HCJF_MATH_CONNECTOR_REGULAR_EXPRESSION)) &&
                trimmedStringValue.matches(SystemProperties.get(SystemProperties.HCJF_MATH_REGULAR_EXPRESSION))) {
            String alias = null;
            String[] asParts = trimmedStringValue.split(SystemProperties.get(SystemProperties.Query.AS_REGULAR_EXPRESSION));
            if(asParts.length == 3) {
                trimmedStringValue = asParts[0].trim();
                alias = asParts[2].trim();
            }

            //If the string matchs with a math expression then creates a function that resolves this math expression.
            String[] mathExpressionParts = trimmedStringValue.split(SystemProperties.get(SystemProperties.HCJF_MATH_SPLITTER_REGULAR_EXPRESSION));
            List<Object> parameters = new ArrayList<>();
            String currentValue;
            boolean desc = false;
            for (int i = 0; i < mathExpressionParts.length; i++) {
                currentValue = mathExpressionParts[i];
                if(i == mathExpressionParts.length - 1){
                    //This code run only one time for the last part.
                    if(parameterClass.equals(QueryReturnParameter.class)) {
                        //Check if the last part contains the 'AS' word
                        String[] parts = currentValue.split(SystemProperties.get(SystemProperties.Query.AS_REGULAR_EXPRESSION));
                        if (parts.length == 2) {
                            currentValue = parts[0].trim();
                            alias = parts[1].trim();
                        }
                    } else if(parameterClass.equals(QueryOrderParameter.class)) {
                        //Check if the last part contains the 'DESC' word
                        String[] parts = currentValue.split(SystemProperties.get(SystemProperties.Query.DESC_REGULAR_EXPRESSION));
                        if(parts.length == 3) {
                            currentValue = parts[0].trim();
                            if(parts[2].trim().equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DESC))) {
                                desc = true;
                            }
                        }
                    }
                }

                if(currentValue.matches(SystemProperties.get(SystemProperties.HCJF_MATH_CONNECTOR_REGULAR_EXPRESSION))) {
                    //If the current value is a math connector (+-*/) the this connector is a function parameter.
                    parameters.add(currentValue.trim());
                } else {
                    //If the current value is not a math connector then this string is evaluated recursively.
                    parameters.add(processStringValue(query, groups, richTexts, currentValue, placesIndex, QueryParameter.class, presentFields));
                }
            }

            if(parameterClass.equals(QueryParameter.class)) {
                result = new QueryFunction(query, Strings.reverseGrouping(trimmedStringValue, groups),
                        SystemProperties.get(SystemProperties.Query.Function.MATH_EVAL_EXPRESSION_NAME), parameters);
            } else if(parameterClass.equals(QueryReturnParameter.class)) {
                result = new QueryReturnFunction(query, Strings.reverseGrouping(trimmedStringValue, groups),
                        SystemProperties.get(SystemProperties.Query.Function.MATH_EVAL_EXPRESSION_NAME), parameters, alias);
            } else if(parameterClass.equals(QueryOrderParameter.class)) {
                result = new QueryOrderFunction(query, Strings.reverseGrouping(trimmedStringValue, groups),
                        SystemProperties.get(SystemProperties.Query.Function.MATH_EVAL_EXPRESSION_NAME), parameters, desc);
            }
        } else {
            //Default case, only must be a query parameter.
            String functionName = null;
            String originalValue = null;
            String replaceValue = null;
            String group = null;
            List<Object> functionParameters = null;
            Boolean function = false;
            Boolean unprocessedValue = false;
            if(trimmedStringValue.contains(Strings.REPLACEABLE_GROUP)) {
                //If the string contains a replaceable group character then the parameter is a function.
                replaceValue = Strings.getGroupIndex(trimmedStringValue, Strings.REPLACEABLE_GROUP);
                group = groups.get(Integer.parseInt(replaceValue.replace(Strings.REPLACEABLE_GROUP,Strings.EMPTY_STRING)));
                functionName = trimmedStringValue.substring(0, trimmedStringValue.indexOf(Strings.REPLACEABLE_GROUP));
                if(functionName == null || functionName.isBlank()) {
                    originalValue = Strings.reverseGrouping(trimmedStringValue, groups);
                    unprocessedValue = true;
                } else {
                    originalValue = Strings.reverseGrouping(trimmedStringValue, groups);
                    functionParameters = new ArrayList<>();
                    for (String param : group.split(SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                        if (!param.isBlank()) {
                            functionParameters.add(processStringValue(query, groups, richTexts, param, placesIndex, parameterClass, presentFields));
                        }
                    }
                    originalValue = Strings.reverseRichTextGrouping(originalValue, richTexts);
                    function = true;
                }
            } else {
                originalValue = trimmedStringValue;
            }

            if(parameterClass.equals(QueryParameter.class)) {
                //If the parameter class is the default class then the result will be a
                //QueryFunction.class instance or QueryField.class instance.
                if(function) {
                    result = new QueryFunction(query, originalValue, functionName, functionParameters);
                } else {
                    result = new QueryField(query, originalValue);
                }
            } else if(parameterClass.equals(QueryReturnParameter.class)) {
                //If the parameter class is the QueryReturnParameter.class then the result will be a
                //QueryReturnFunction.class instance or QueryReturnField.class instance.
                String alias = null;
                String[] parts = originalValue.split(SystemProperties.get(SystemProperties.Query.AS_REGULAR_EXPRESSION));
                if(parts.length == 3) {
                    originalValue = parts[0].trim();
                    alias = parts[2].trim();
                }

                if(function) {
                    result = new QueryReturnFunction(query, originalValue, functionName, functionParameters, alias);
                } else if(unprocessedValue) {
                    if(alias == null) {
                        throw new HCJFRuntimeException("Unable to create a unprocessed value without alias");
                    }
                    Object newValue = processStringValue(query, groups, richTexts, replaceValue, placesIndex, parameterClass, presentFields);
                    if(newValue instanceof QueryReturnParameter) {
                        result = newValue;
                    } else if(newValue instanceof BaseEvaluator.UnprocessedValue) {
                        result = new QueryReturnUnprocessedValue(query, originalValue, alias, (BaseEvaluator.UnprocessedValue) newValue);
                    } else {
                        result = new QueryReturnField(query, (String) newValue, alias);
                    }
                } else {
                    result = new QueryReturnField(query, originalValue, alias);
                }
            } else if(parameterClass.equals(QueryOrderParameter.class)) {
                //If the parameter class is the QueryOrderParameter.class then the result will be a
                //QueryOrderFunction.class instance or QueryOrderField.class instance.
                boolean desc = false;
                String[] parts = originalValue.split(SystemProperties.get(SystemProperties.Query.DESC_REGULAR_EXPRESSION));
                if(parts.length == 2) {
                    originalValue = parts[0].trim();
                    if(parts[1].trim().equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DESC))) {
                        desc = true;
                    }
                }

                if(function) {
                    result = new QueryOrderFunction(query, originalValue, functionName, functionParameters, desc) ;
                } else {
                    result = new QueryOrderField(query, originalValue, desc);
                }
            }
        }

        if(result instanceof QueryField) {
            presentFields.add((QueryField) result);
        }

        return result;
    }
}
