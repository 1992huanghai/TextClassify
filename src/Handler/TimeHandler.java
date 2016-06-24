package Handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TimeHandler implements InvocationHandler {
	public long time=0;

	public TimeHandler(Object target) {
		super();
		this.target = target;
	}

	private Object target;
	
	/*
	 * 参数：
	 * proxy  被代理对象
	 * method  被代理对象的方法
	 * args 方法的参数
	 * 
	 * 返回值：
	 * Object  方法的返回值
	 * */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		long starttime = System.currentTimeMillis();
		method.invoke(target);
		long endtime = System.currentTimeMillis();
		this.time=endtime-starttime;
		return null;
	}

}

