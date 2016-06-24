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
	 * ������
	 * proxy  ���������
	 * method  ���������ķ���
	 * args �����Ĳ���
	 * 
	 * ����ֵ��
	 * Object  �����ķ���ֵ
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

