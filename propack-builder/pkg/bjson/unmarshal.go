package bjson

import (
	"encoding/json"
	"reflect"
)

func Unmarshal(data []byte, v any) error {
	rv := reflect.ValueOf(v)
	if rv.Kind() != reflect.Pointer || rv.IsNil() {
		return &json.InvalidUnmarshalError{Type: reflect.TypeOf(v)}
	}

	return json.Unmarshal(data, v)
}

func (j JsonObject) Deserialize(data []byte) error {
	return json.Unmarshal(data, &j)
}

func (j JsonArray) Deserialize(data []byte) error {
	return json.Unmarshal(data, &j)
}
