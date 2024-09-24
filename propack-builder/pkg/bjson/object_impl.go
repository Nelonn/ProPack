package bjson

func (j JsonObject) Object() JsonObject {
	return j
}

func (j JsonObject) Array() JsonArray {
	panic("Not array")
}

func (j JsonObject) String() string {
	panic("Not string")
}

func (j JsonObject) Byte() byte {
	panic("Not byte")
}

func (j JsonObject) Short() int16 {
	panic("Not short")
}

func (j JsonObject) Int() int32 {
	panic("Not int")
}

func (j JsonObject) Long() int64 {
	panic("Not long")
}

func (j JsonObject) Float() float32 {
	panic("Not float")
}

func (j JsonObject) Double() float64 {
	panic("Not double")
}
