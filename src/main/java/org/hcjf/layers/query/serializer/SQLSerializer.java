package org.hcjf.layers.query.serializer;

import org.hcjf.layers.Layer;
import org.hcjf.layers.query.Join;
import org.hcjf.layers.query.Query;
import org.hcjf.layers.query.Queryable;
import org.hcjf.layers.query.evaluators.*;
import org.hcjf.layers.query.model.QueryOrderParameter;
import org.hcjf.layers.query.model.QueryReturnParameter;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Strings;

import java.util.Collection;
import java.util.Date;

public class SQLSerializer extends Layer implements QuerySerializer {

    private static final String NAME = "SQL";

    @Override
    public String getImplName() {
        return NAME;
    }

    /**
     * This method creates query expression from a query instance.
     *
     * @param query Query instance.
     * @return Query expression.
     */
    @Override
    public String serialize(Query query){
        Strings.Builder resultBuilder = new Strings.Builder();

        //Print select
        resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.SELECT));
        resultBuilder.append(Strings.WHITE_SPACE);
        if (query.returnAll()) {
            resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.RETURN_ALL));
            SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR);
            resultBuilder.append(Strings.WHITE_SPACE);
        }
        for (QueryReturnParameter field : query.getReturnParameters()) {
            resultBuilder.append(field);
            if (field.getAlias() != null) {
                resultBuilder.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.AS));
                resultBuilder.append(Strings.WHITE_SPACE).append(field.getAlias());
            }
            resultBuilder.append(Strings.EMPTY_STRING, SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR));
        }
        resultBuilder.cleanBuffer();

        //Print from
        resultBuilder.append(Strings.WHITE_SPACE);
        resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.FROM));
        resultBuilder.append(Strings.WHITE_SPACE);
        resultBuilder.append(query.getResource().toString());
        resultBuilder.append(Strings.WHITE_SPACE);

        //Print joins
        for (Join join : query.getJoins()) {
            if (!(join.getType() == Join.JoinType.JOIN)) {
                resultBuilder.append(join.getType());
                resultBuilder.append(Strings.WHITE_SPACE);
            }
            resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.JOIN)).append(Strings.WHITE_SPACE);
            resultBuilder.append(join.getResource().toString()).append(Strings.WHITE_SPACE);
            resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.ON)).append(Strings.WHITE_SPACE);
            if (join.getEvaluators().size() > 0) {
                toStringEvaluatorCollection(resultBuilder, join);
            }
        }

        if (query.getEvaluators().size() > 0) {
            resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.WHERE)).append(Strings.WHITE_SPACE);
            toStringEvaluatorCollection(resultBuilder, query);
        }

        if (query.getGroupParameters().size() > 0) {
            resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.GROUP_BY)).append(Strings.WHITE_SPACE);
            for (QueryReturnParameter groupParameter : query.getGroupParameters()) {
                resultBuilder.append(groupParameter, SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR));
            }
            resultBuilder.cleanBuffer();
            resultBuilder.append(Strings.WHITE_SPACE);
        }

        if (query.getOrderParameters().size() > 0) {
            resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.ORDER_BY)).append(Strings.WHITE_SPACE);
            for (QueryOrderParameter orderField : query.getOrderParameters()) {
                resultBuilder.append(orderField);
                if (orderField.isDesc()) {
                    resultBuilder.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.DESC));
                }
                resultBuilder.append(Strings.EMPTY_STRING, SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR));
            }
            resultBuilder.cleanBuffer();
        }

        if (query.getStart() != null) {
            resultBuilder.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.START));
            resultBuilder.append(Strings.WHITE_SPACE).append(query.getStart());
        }

        if (query.getUnderlyingStart() != null) {
            if(query.getStart() == null) {
                resultBuilder.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.START)).append(Strings.WHITE_SPACE);
            }
            resultBuilder.append(Strings.ARGUMENT_SEPARATOR).append(query.getUnderlyingStart());
        }

        if (query.getLimit() != null) {
            resultBuilder.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.LIMIT));
            resultBuilder.append(Strings.WHITE_SPACE).append(query.getLimit());
        }

        if (query.getUnderlyingLimit() != null) {
            if(query.getLimit() == null) {
                resultBuilder.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.LIMIT)).append(Strings.WHITE_SPACE);
            }
            resultBuilder.append(Strings.ARGUMENT_SEPARATOR).append(query.getUnderlyingLimit());
        }

        for(Queryable queryable : query.getUnions()) {
            resultBuilder.append(Strings.WHITE_SPACE);
            resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.UNION));
            resultBuilder.append(Strings.WHITE_SPACE);
            resultBuilder.append(queryable.toString());
        }

        return resultBuilder.toString();
    }

    /**
     * Creates a string representation of evaluator collection.
     * @param result Buffer with the current result.
     * @param collection Collection in order to create the string representation.
     */
    private void toStringEvaluatorCollection(Strings.Builder result, EvaluatorCollection collection) {
        String separator = Strings.EMPTY_STRING;
        String separatorValue = collection instanceof Or ?
                SystemProperties.get(SystemProperties.Query.ReservedWord.OR) :
                SystemProperties.get(SystemProperties.Query.ReservedWord.AND);
        for(Evaluator evaluator : collection.getEvaluators()) {
            if(evaluator instanceof Or) {
                if(!separator.isEmpty()) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.OR));
                }
                result.append(Strings.WHITE_SPACE);
                if(((Or)evaluator).getEvaluators().size() == 1) {
                    toStringEvaluatorCollection(result, (Or) evaluator);
                } else {
                    result.append(Strings.START_GROUP);
                    toStringEvaluatorCollection(result, (Or) evaluator);
                    result.append(Strings.END_GROUP);
                }
                result.append(Strings.WHITE_SPACE);
            } else if(evaluator instanceof And) {
                if(!separator.isEmpty()) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.AND));
                }
                result.append(Strings.WHITE_SPACE);
                if (collection instanceof Query) {
                    toStringEvaluatorCollection(result, (And) evaluator);
                } else {
                    if (((And) evaluator).getEvaluators().size() == 1) {
                        toStringEvaluatorCollection(result, (And) evaluator);
                    } else {
                        result.append(Strings.START_GROUP);
                        toStringEvaluatorCollection(result, (And) evaluator);
                        result.append(Strings.END_GROUP);
                    }
                }
                result.append(Strings.WHITE_SPACE);
            } else if(evaluator instanceof BooleanEvaluator) {
                result.append(separator);
                BooleanEvaluator booleanEvaluator = (BooleanEvaluator) evaluator;
                if(booleanEvaluator.isTrueForced()) {
                    result.append(Boolean.TRUE.toString());
                } else {
                    result = toStringFieldEvaluatorValue(booleanEvaluator.getValue(), booleanEvaluator.getClass(), result);
                }
                result.append(Strings.WHITE_SPACE);
            } else if(evaluator instanceof FieldEvaluator) {
                result.append(separator);
                FieldEvaluator fieldEvaluator = (FieldEvaluator) evaluator;
                if(fieldEvaluator.isTrueForced()) {
                    result.append(Boolean.TRUE.toString());
                } else {
                    if (fieldEvaluator.getLeftValue() == null) {
                        result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.NULL));
                    } else {
                        result = toStringFieldEvaluatorValue(fieldEvaluator.getLeftValue(), fieldEvaluator.getLeftValue().getClass(), result);
                    }
                    result.append(Strings.WHITE_SPACE);
                    if (fieldEvaluator instanceof Distinct) {
                        result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.DISTINCT)).append(Strings.WHITE_SPACE);
                    } else if (fieldEvaluator instanceof Equals) {
                        result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.EQUALS)).append(Strings.WHITE_SPACE);
                    } else if (fieldEvaluator instanceof GreaterThanOrEqual) {
                        result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN_OR_EQUALS)).append(Strings.WHITE_SPACE);
                    } else if (fieldEvaluator instanceof GreaterThan) {
                        result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN)).append(Strings.WHITE_SPACE);
                    } else if (fieldEvaluator instanceof NotIn) {
                        result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.NOT_IN)).append(Strings.WHITE_SPACE);
                    } else if (fieldEvaluator instanceof In) {
                        result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.IN)).append(Strings.WHITE_SPACE);
                    } else if (fieldEvaluator instanceof Like) {
                        result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.LIKE)).append(Strings.WHITE_SPACE);
                    } else if (fieldEvaluator instanceof SmallerThanOrEqual) {
                        result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN_OR_EQUALS)).append(Strings.WHITE_SPACE);
                    } else if (fieldEvaluator instanceof SmallerThan) {
                        result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN)).append(Strings.WHITE_SPACE);
                    }
                    if (fieldEvaluator.getRightValue() == null) {
                        result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.NULL));
                    } else {
                        result = toStringFieldEvaluatorValue(fieldEvaluator.getRightValue(), fieldEvaluator.getRightValue().getClass(), result);
                    }
                }
                result.append(Strings.WHITE_SPACE);
            }
            separator = separatorValue + Strings.WHITE_SPACE;
        }
    }

    /**
     * Creates the string representation of the field evaluator.
     * @param value Object to create the string representation.
     * @param type Object type.
     * @param result Buffer with the current result.
     * @return String representation of the field evaluator.
     */
    private static Strings.Builder toStringFieldEvaluatorValue(Object value, Class type, Strings.Builder result) {
        if(FieldEvaluator.ReplaceableValue.class.isAssignableFrom(type)) {
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.REPLACEABLE_VALUE));
        } else if(FieldEvaluator.QueryValue.class.isAssignableFrom(type)) {
            result.append(Strings.START_GROUP);
            result.append(((FieldEvaluator.QueryValue)value).getQuery().toString());
            result.append(Strings.END_GROUP);
        } else if(String.class.isAssignableFrom(type)) {
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.STRING_DELIMITER));
            result.append(value);
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.STRING_DELIMITER));
        } else if(Date.class.isAssignableFrom(type)) {
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.STRING_DELIMITER));
            result.append(SystemProperties.getDateFormat(SystemProperties.Query.DATE_FORMAT).format((Date)value));
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.STRING_DELIMITER));
        } else if(Collection.class.isAssignableFrom(type)) {
            result.append(Strings.START_GROUP);
            String separator = Strings.EMPTY_STRING;
            for(Object object : (Collection)value) {
                if(object != null) {
                    result.append(separator);
                    result = toStringFieldEvaluatorValue(object, object.getClass(), result);
                    separator = SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR);
                }
            }
            result.append(Strings.END_GROUP);
        } else {
            result.append(value.toString());
        }
        return result;
    }
}
