package vecs

import "math"

type Vec3f struct {
	X, Y, Z float32
}

// set

func (v Vec3f) set(x, y, z float32) {
	v.X = x
	v.Y = y
	v.Z = z
}

func (v Vec3f) setVec(other Vec3f) {
	v.set(other.X, other.Y, other.Z)
}

func (v Vec3f) setAll(all float32) {
	v.set(all, all, all)
}

// add

func (v Vec3f) add(x, y, z float32) {
	v.X += x
	v.Y += y
	v.Z += z
}

func (v Vec3f) addVec(other Vec3f) {
	v.add(other.X, other.Y, other.Z)
}

func (v Vec3f) addAll(all float32) {
	v.add(all, all, all)
}

// subtract

func (v Vec3f) sub(x, y, z float32) {
	v.X -= x
	v.Y -= y
	v.Z -= z
}

func (v Vec3f) subVec(other Vec3f) {
	v.sub(other.X, other.Y, other.Z)
}

func (v Vec3f) subAll(all float32) {
	v.sub(all, all, all)
}

// multiply

func (v Vec3f) mul(x, y, z float32) {
	v.X *= x
	v.Y *= y
	v.Z *= z
}

func (v Vec3f) mulVec(other Vec3f) {
	v.mul(other.X, other.Y, other.Z)
}

func (v Vec3f) mulAll(all float32) {
	v.mul(all, all, all)
}

// divide

func (v Vec3f) div(x, y, z float32) {
	v.X /= x
	v.Y /= y
	v.Z /= z
}

func (v Vec3f) divVec(other Vec3f) {
	v.div(other.X, other.Y, other.Z)
}

func (v Vec3f) divAll(all float32) {
	v.div(all, all, all)
}

// length

func (v Vec3f) lenSqr() float32 {
	return v.X*v.X + v.Y*v.Y + v.Z*v.Z
}

func (v Vec3f) len() float32 {
	return float32(math.Sqrt(float64(v.lenSqr())))
}

// other

func (v Vec3f) normalize() {
	length := v.len()
	if length < 1.0e-4 {
		v.setAll(0)
	} else {
		v.divAll(length)
	}
}

func (v Vec3f) negate() {
	v.X = -v.X
	v.Y = -v.Y
	v.Z = -v.Z
}
