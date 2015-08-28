package ch.ntb.inf.kmip.client;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import ch.ntb.inf.kmip.attributes.*;
import ch.ntb.inf.kmip.container.*;
import ch.ntb.inf.kmip.kmipenum.*;
import ch.ntb.inf.kmip.objects.base.*;
import ch.ntb.inf.kmip.stub.KMIPStub;


public class SimpleKMIPClient {

	// initialize Logger
	private static final Logger logger = Logger.getLogger(SimpleKMIPClient.class);
	
	public static void main(String[] args) {
		// configure Logger
		DOMConfigurator.configureAndWatch( "config/log4j-1.2.17.xml", 60*1000 );
		logger.info("Hello KMIPClient! What a beatiful day;)");
		
		KMIPStub stub = new KMIPStub();
		KMIPContainer request = createKMIPRequest();
		KMIPContainer response = stub.processRequest(request);
		System.out.println(response.toString());
	}

	private static KMIPContainer createKMIPRequest() {
		// Create Container with one Batch
		KMIPContainer container = new KMIPContainer();
		KMIPBatch batch = new KMIPBatch();
		container.addBatch(batch);
		container.calculateBatchCount();
		
		// Set Operation and Attribute
		batch.setOperation(EnumOperation.Create);
		batch.addAttribute(new ObjectType(EnumObjectType.SymmetricKey));
		
		// Set Template Attribute
		ArrayList<Attribute> templateAttributes = new ArrayList<Attribute>();
		templateAttributes.add(new CryptographicAlgorithm(EnumCryptographicAlgorithm.AES));
		templateAttributes.add(new CryptographicLength(128));
		templateAttributes.add(new CryptographicUsageMask(0x0C));
		TemplateAttributeStructure tas = new TemplateAttribute();
		tas.setAttributes(templateAttributes);
		batch.addTemplateAttributeStructure(tas);

		return container;
	}

}
