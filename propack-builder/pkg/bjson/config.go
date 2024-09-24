package bjson

import "reflect"

type Adapter struct {
	Deserialize func(j JsonElement) any
	Serialize   func(v any) JsonElement
}

type BJSON struct {
	PrettyPrinting bool
	Adapters       map[reflect.Type]Adapter
}

var Default = BJSON{}
