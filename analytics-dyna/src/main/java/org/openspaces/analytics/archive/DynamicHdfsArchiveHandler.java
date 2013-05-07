package org.openspaces.analytics.archive;

import java.util.logging.Logger;

import org.openspaces.archiver.hdfs.HdfsArchiveHandler;
import org.openspaces.archiver.hdfs.HdfsSerializer;


public class DynamicHdfsArchiveHandler  extends HdfsArchiveHandler implements DiscardingArchiveHandler {
	private static final Logger log=Logger.getLogger(DynamicHdfsArchiveHandler.class.getName());
	private boolean discard=false;
	
	public DynamicHdfsArchiveHandler(HdfsSerializer serializer) {
		super(serializer);
		log.info("constructing");
	}

	@Override
	public void setDiscard(boolean discard) {
		this.discard=discard;
		log.info("setting discard=="+discard);
	}

	@Override
	public void archive(Object... objs) {
		log.info(String.format("got %d events",objs.length));
		if(!discard){
			super.archive(objs);
		}
		else{
			log.info("discarding "+objs.length+" events");
		}
	}
	
}
