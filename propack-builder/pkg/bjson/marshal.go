package bjson

import "encoding/json"

func (j JsonObject) Serialize() ([]byte, error) {
	return json.Marshal(j)
}

func (j JsonArray) Serialize() ([]byte, error) {
	return json.Marshal(j)
}
