package bjson

func (j JsonString) Object() JsonObject {
	panic("Not object")
}

func (j JsonString) Array() JsonArray {
	panic("Not array")
}

func (j JsonString) String() string {
	return j.Value
}

func (j JsonString) Byte() byte {
	panic("Not byte")
}

func (j JsonString) Short() int16 {
	panic("Not short")
}

func (j JsonString) Int() int32 {
	panic("Not int")
}

func (j JsonString) Long() int64 {
	panic("Not long")
}

func (j JsonString) Float() float32 {
	panic("Not float")
}

func (j JsonString) Double() float64 {
	panic("Not double")
}
