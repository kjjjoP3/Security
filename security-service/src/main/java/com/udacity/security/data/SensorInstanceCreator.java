package com.udacity.security.data;

import com.google.gson.InstanceCreator;
import java.lang.reflect.Type;

public class SensorInstanceCreator implements InstanceCreator<Sensor> {
	@Override
	public Sensor createInstance(Type type) {
		return new Sensor();
	}
}
