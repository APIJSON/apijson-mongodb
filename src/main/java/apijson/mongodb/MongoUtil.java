/*Copyright ©2024 APIJSON(https://github.com/APIJSON)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package apijson.mongodb;

import com.mongodb.jdbc.MongoBsonValue;
import org.bson.*;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;


/**
 * @author Lemon
 * @see DemoSQLExecutor 重写 getValue 方法：
 *     \@Override
 *     protected Object getValue(SQLConfig<Long> config, ResultSet rs, ResultSetMetaData rsmd, int tablePosition
 *         , JSONObject table, int columnIndex, String lable, Map<String, JSONObject> childMap) throws Exception {
 *         Object v = super.getValue(config, rs, rsmd, tablePosition, table, columnIndex, lable, childMap);
 *         return MongoUtil.getValue(v);
 *     }
 */
public class MongoUtil {
    public static final String TAG = "MongoUtil";

    /**去除多余的包装
     * @param value
     * @return
     */
    public static Object getValue(Object value) {
        if (value instanceof Bson) {
            try {
                value = ((Bson) value).toBsonDocument();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        if (value instanceof MongoBsonValue) {
            value = ((MongoBsonValue) value).getBsonValue();
        }



        if (value instanceof BsonValue) {
            BsonValue nv = (BsonValue) value;
            if (nv.isNull()) {
                return null;
            }

            if (nv.isArray()) {
                List<BsonValue> vs = nv.asArray().getValues();
                if (vs == null) {
                    return null;
                }

                List<Object> l = new ArrayList<>();
                for (BsonValue v : vs) {
                    Object cv = getValue(v);
                    l.add(cv);
                }

                return l;
            }

            if (nv.isDocument()) {
                BsonDocument bd = nv.asDocument();
                Set<Map.Entry<String, BsonValue>> set = bd == null ? null : bd.entrySet();
                if (set == null) {
                    return null;
                }

                Map<Object, Object> m = new LinkedHashMap<>();
                for (Map.Entry<String, BsonValue> ety : set) {
                    Object cv = getValue(ety.getValue());
                    m.put(ety.getKey(), cv);
                }

                return m;
            }

            if (nv.isDBPointer()) {
                value = nv.asDBPointer().getId();
            }

            if (nv.isBoolean()) {
                return nv.asBoolean().getValue();
            }
            if (nv.isBinary()) {
                return nv.asBinary().getData();
            }
            if (nv.isInt32()) {
                return nv.asInt32().getValue();
            }
            if (nv.isInt64()) {
                return nv.asInt64().getValue();
            }
            if (nv.isDouble()) {
                return nv.asDouble().getValue();
            }
            if (nv.isNumber()) {
                return nv.asNumber().doubleValue();
            }
            if (nv.isDecimal128()) {
                return nv.asDecimal128().doubleValue();
            }
            if (nv.isObjectId()) {
                ObjectId v = nv.asObjectId().getValue();
                return v == null ? null : v.toString();
            }
            if (nv.isDateTime()) {
                long v = nv.asDateTime().getValue();
                return v; // new Timestamp(v);
            }
            if (nv.isString()) {
                return nv.asString().getValue();
            }
            if (nv.isSymbol()) {
                return nv.asSymbol().getSymbol();
            }

            if (nv.isJavaScript()) {
                BsonJavaScript v = nv.asJavaScript();
                if (v == null) {
                    return null;
                }

                Map<Object, Object> m = new LinkedHashMap<>();
                m.put("type", BsonType.JAVASCRIPT);
                m.put("code", v.getCode());
                return m;
            }

            if (nv.isJavaScriptWithScope()) {
                BsonJavaScriptWithScope v = nv.asJavaScriptWithScope();
                if (v == null) {
                    return null;
                }

                Map<Object, Object> m = new LinkedHashMap<>();
                m.put("type", BsonType.JAVASCRIPT);
                m.put("scope", getValue(v.getScope()));
                m.put("code", v.getCode());
                return m;
            }
        }

        return value;
    }
}
