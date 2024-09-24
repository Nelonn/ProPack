package bjson

func (j JsonArray) Object() JsonObject {
	panic("Not object")
}

func (j JsonArray) Array() JsonArray {
	return j
}

func (j JsonArray) String() string {
	panic("Not string")
}

func (j JsonArray) Byte() byte {
	panic("Not byte")
}

func (j JsonArray) Short() int16 {
	panic("Not short")
}

func (j JsonArray) Int() int32 {
	panic("Not int")
}

func (j JsonArray) Long() int64 {
	panic("Not long")
}

func (j JsonArray) Float() float32 {
	panic("Not float")
}

func (j JsonArray) Double() float64 {
	panic("Not double")
}
