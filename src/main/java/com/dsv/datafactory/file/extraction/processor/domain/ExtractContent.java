package com.dsv.datafactory.file.extraction.processor.domain;


import com.dsv.datafactory.file.extraction.processor.domain.readers.ReadImage;
import com.dsv.datafactory.file.extraction.processor.domain.saving.DocumentToDisk;
import com.dsv.datafactory.file.extraction.processor.logging.ECSLoggerProvider;
import com.dsv.datafactory.model.Document;
import com.dsv.datafactory.model.MetaData;
import com.dsv.logger.ECSLogger;

import javax.inject.Inject;


public class ExtractContent {

	private final ReadImage extractor;
	private final DocumentToDisk documentToDisk;

	@Inject
	public ExtractContent(ReadImage extractor, DocumentToDisk documentToDisk) {
		this.extractor = extractor;
		this.documentToDisk = documentToDisk;
	}

	private final ECSLogger logger = ECSLoggerProvider.getLogger(ExtractContent.class.getName());

	public MetaData execute(MetaData metaData) {

		logger.info("key: " + metaData.key);

		try {

			Document parsedDocument = extractor.extract(metaData.sortedImagePaths,metaData.key);

			logger.info("Saving hocr to disk");
			documentToDisk.execute(metaData.key,parsedDocument,metaData.shipmentId);

			metaData.extractedOCRDocumentPath =  parsedDocument.getPathToDocumentFile();
			logger.info("Setting metaData paths to " + metaData.extractedOCRDocumentPath);
			return metaData;

		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}


	}
}
