package com.gigaspaces.analytics;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.query.IdQuery;

public class NetChangeSetTest {
	private static UrlSpaceConfigurer usc;
	private static GigaSpace space;
	
	@BeforeClass
	public static void beforeClass(){
		usc=new UrlSpaceConfigurer("/./space");
		space=new GigaSpaceConfigurer(usc.space()).gigaSpace();
	}
	
	@AfterClass
	public static void afterClass()throws Exception{
		usc.destroy();
	}
	
	@Before
	public void before(){
		space.clear(new Object());
	}

	@Test
	public void testCount() {
		AccumulatorChangeSet cs=new AccumulatorChangeSet();
		cs.increment("fint",1);
		org.junit.Assert.assertEquals(1,cs.getChangeCount());
		cs.increment("flong",(long)1);
		org.junit.Assert.assertEquals(2,cs.getChangeCount());
		cs.increment("fdouble",(double)1);
		org.junit.Assert.assertEquals(3,cs.getChangeCount());
		cs.increment("ffloat",(float)1);
		org.junit.Assert.assertEquals(4,cs.getChangeCount());
		cs.increment("fshort",(short)1);
		org.junit.Assert.assertEquals(5,cs.getChangeCount());
		cs.increment("fbyte",(byte)1);
		org.junit.Assert.assertEquals(6,cs.getChangeCount());
		cs.decrement("fint",1);
		org.junit.Assert.assertEquals(7,cs.getChangeCount());
		cs.decrement("flong",(long)1);
		org.junit.Assert.assertEquals(8,cs.getChangeCount());
		cs.decrement("fdouble",(double)1);
		org.junit.Assert.assertEquals(9,cs.getChangeCount());
		cs.decrement("ffloat",(float)1);
		org.junit.Assert.assertEquals(10,cs.getChangeCount());
		cs.decrement("fshort",(short)1);
		org.junit.Assert.assertEquals(11,cs.getChangeCount());
		cs.decrement("fbyte",(byte)1);
		org.junit.Assert.assertEquals(12,cs.getChangeCount());
	}
	
	/**
	 * Note that byte and short are ommited since they don't work
	 * in the XAP API
	 * 
	 */
	@Test
	public void testNet(){
		//space.write(new CTest(0));
		space.write(new Accumulator("test"));
		AccumulatorChangeSet cs=new AccumulatorChangeSet();
		
		cs.increment("fint",1);
		cs.increment("fint",1);
		cs.decrement("fint",1);
		cs.increment("flong",1L);
		cs.increment("flong",1L);
		cs.decrement("flong",1L);
		cs.increment("fdouble",1D);
		cs.increment("fdouble",1D);
		cs.decrement("fdouble",1D);
		cs.increment("ffloat",1F);
		cs.increment("ffloat",1F);
		cs.decrement("ffloat",1F); 
		
		cs.summarize();
//		space.change(new IdQuery<CTest>(CTest.class,0),cs);
		space.change(new IdQuery<Accumulator>(Accumulator.class,"test"),cs);
		
//		CTest after=space.read(new CTest());
		Accumulator after=space.read(new Accumulator());
		org.junit.Assert.assertNotNull(after);
/*		org.junit.Assert.assertEquals((int)1,(int)after.getFint());
		org.junit.Assert.assertEquals((long)1,(long)after.getFlong());
		org.junit.Assert.assertEquals(1D,after.getFdouble(),.001D);
		org.junit.Assert.assertEquals(1F,after.getFint(),.001F);*/ 
		org.junit.Assert.assertEquals(1,after.getValues().get("fint"));
		org.junit.Assert.assertEquals(1L,after.getValues().get("flong"));
		org.junit.Assert.assertEquals(1D,(Double)after.getValues().get("fdouble"),.001D);
		org.junit.Assert.assertEquals(1F,(Float)after.getValues().get("ffloat"),.001F); 
	}
	

	@SpaceClass
	public static class CTest{
		private Integer fint;
		private Long flong;
		private Double fdouble;
		private Float ffloat;
		private Short fshort;
		private Byte fbyte;
		
		public Integer getFint() {
			return fint;
		}
		public void setFint(Integer fint) {
			this.fint = fint;
		}
		public Long getFlong() {
			return flong;
		}
		public void setFlong(Long flong) {
			this.flong = flong;
		}
		public Double getFdouble() {
			return fdouble;
		}
		public void setFdouble(Double fdouble) {
			this.fdouble = fdouble;
		}
		public Float getFfloat() {
			return ffloat;
		}
		public void setFfloat(Float ffloat) {
			this.ffloat = ffloat;
		}
		public Short getFshort() {
			return fshort;
		}
		public void setFshort(Short fshort) {
			this.fshort = fshort;
		}
		public Byte getFbyte() {
			return fbyte;
		}
		public void setFbyte(Byte fbyte) {
			this.fbyte = fbyte;
		}

		private Integer id;
		
		public CTest(){}
		public CTest(int id){this.id=id;}

		@SpaceId
		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		} 
	}
}
