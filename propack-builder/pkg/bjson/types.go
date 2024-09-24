package bjson

type JsonObject map[string]interface{}
type JsonArray []interface{}

type JsonElement interface {
	Object() JsonObject
	Array() JsonArray
	String() string
	Byte() byte
	Short() int16
	Int() int32
	Long() int64
	Float() float32
	Double() float64
}

func typeOf(value interface{}) string {
	switch value.(type) {
	case string:
		return "string"
	case float64:
		return "number"
	case map[string]interface{}:
		return "object"
	case []interface{}:
		return "array"
	default:
		return "unknown"
	}
}
