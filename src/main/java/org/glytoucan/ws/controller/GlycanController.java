package org.glytoucan.ws.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
//import org.eurocarbdb.MolecularFramework.util.similiarity.SearchEngine.SearchEngineException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConversion;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;
import org.glytoucan.ws.api.Confirmation;
import org.glytoucan.ws.api.ErrorCodes;
import org.glytoucan.ws.api.Glycan;
import org.glytoucan.ws.api.GlycanErrorResponse;
import org.glytoucan.ws.api.GlycanInput;
import org.glytoucan.ws.api.GlycanInputList;
import org.glytoucan.ws.api.GlycanList;
import org.glytoucan.ws.api.GlycanResponse;
import org.glytoucan.ws.api.GlycanResponseList;
import org.glytoucan.ws.api.User;
import org.glytoucan.ws.api.UserInput;
import org.glytoucan.ws.service.EmailManager;
import org.glytoucan.ws.service.GlycanManager;
import org.glytoucan.ws.service.UserManager;
import org.glytoucan.ws.service.exception.GlycanNotFoundException;
import org.glytoucan.ws.service.exception.UserQuotaExceededException;
import org.slf4j.Logger;
//import org.glycomedb.rdf.glycordf.util.GlycoRDFWriter;
//import org.glycomedb.rdf.glycordf.util.RDFGeneratorGlycanConfig;
//import org.glycomedb.residuetranslator.ResidueTranslator;
//import org.glytoucan.ws.dao.exceptions.ErrorCodes;
//import org.glytoucan.ws.dao.exceptions.GlycanNotFoundException;
//import org.glytoucan.ws.dao.exceptions.UserQuotaExceededException;
//import org.glytoucan.ws.database.GlycanComposition;
//import org.glytoucan.ws.database.GlycanEntity;
//import org.glytoucan.ws.database.MotifEntity;
//import org.glytoucan.ws.database.MotifTag;
//import org.glytoucan.ws.database.UserEntity;
//import org.glytoucan.ws.importers.GWSImporter;
//import org.glytoucan.ws.service.EmailManager;
//import org.glytoucan.ws.service.GlycanManager;
//import org.glytoucan.ws.service.UserManager;
//import org.glytoucan.ws.service.search.CombinationSearch;
//import org.glytoucan.ws.utils.GlycanStructureProvider;
//import org.glytoucan.ws.utils.ImageGenerator;
//import org.glytoucan.ws.utils.MassCalculator;
//import org.glytoucan.ws.view.Confirmation;
//import org.glytoucan.ws.view.GlycanErrorResponse;
//import org.glytoucan.ws.view.GlycanExhibit;
//import org.glytoucan.ws.view.GlycanInputList;
//import org.glytoucan.ws.view.GlycanList;
//import org.glytoucan.ws.view.GlycanResponseList;
//import org.glytoucan.ws.view.StructureParserValidator;
//import org.glytoucan.ws.view.User;
//import org.glytoucan.ws.view.search.CompositionSearchInput;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Controller
@Api(value="/glycans", description="Structure Management")
@RequestMapping ("/glycans")
public class GlycanController {
	
	public static Logger logger=(Logger) LoggerFactory.getLogger("org.glytoucan.ws.dao.GlycanController");

//	@Autowired
	GlycanManager glycanManager;
//	
//	@Autowired
	EmailManager emailManager;
//	
//	@Autowired
	UserManager userManager;
//	
//	@Autowired
//	ImageGenerator imageGenerator;
//	
//	@Autowired
//	MassCalculator massCalculator;
	
//	@Autowired
	MonosaccharideConversion residueTranslator;
	
//	@Autowired 
	MonosaccharideConverter monosaccharideConverter;
	
//	@Value("${documentation.services.basePath}")
	String serverBasePath;

//	public void setGlycanManager(GlycanManager glycanManager) {
//		this.glycanManager = glycanManager;
//	}
	
//	private RDFGeneratorGlycanConfig createRDFConfiguration() {
//		RDFGeneratorGlycanConfig t_config = new RDFGeneratorGlycanConfig();
//        t_config.setReferencedCompound(false);
//        t_config.setImages(true);
//        t_config.setRemoteEntries(true);
//        t_config.setSequenceGlycoCt(true);
//        t_config.setSequenceGlydeII(false);
//        t_config.setSequenceKCF(false);
//        t_config.setSequenceLinucs(false);
//        t_config.setComposition(true);
//        t_config.setSequenceCarbBank(false);
//        t_config.setMotif(true);
//        t_config.setFlatReferencedCompound(false);
//        t_config.setFlatSequence(false);
//        return t_config;
//	}
	
//	public Sugar importParseValidate (Glycan glycan) throws GlycoVisitorException, SugarImporterException {
//		String encoding = glycan.getEncoding();
//		logger.debug("Input structure: {}", glycan.getStructure());
//		Sugar sugarStructure = null;
//		if (encoding != null && !encoding.isEmpty() && !(encoding.equalsIgnoreCase("glycoct") || encoding.equalsIgnoreCase("glycoct_condensed"))  && !encoding.equalsIgnoreCase("gws")) {
//			logger.debug("Converting from {}", encoding);
//			ArrayList<CarbohydrateSequenceEncoding> supported = SugarImporterFactory.getSupportedEncodings();
//			for (Iterator<CarbohydrateSequenceEncoding> iterator = supported.iterator(); iterator.hasNext();) {
//				CarbohydrateSequenceEncoding carbohydrateSequenceEncoding = (CarbohydrateSequenceEncoding) iterator
//						.next();
//				if (encoding.equalsIgnoreCase(carbohydrateSequenceEncoding.getId())) {	
//					try {
//						if (encoding.equalsIgnoreCase("kcf")) {
//							sugarStructure = SugarImporterFactory.importSugar(glycan.getStructure(), carbohydrateSequenceEncoding, residueTranslator);
//						}
//						else {
//							sugarStructure = SugarImporterFactory.importSugar(glycan.getStructure(), carbohydrateSequenceEncoding, monosaccharideConverter);
//						}
//					} catch (Exception e) {
//						// import failed
//						String message=e.getMessage();
//						//e.printStackTrace();
//						if (e instanceof SugarImporterException) {
//							message = ((SugarImporterException)e).getErrorText() + ": " + ((SugarImporterException)e).getPosition();
//						}
//						throw new IllegalArgumentException("Structure cannot be imported: " + message);
//					}
//					break;
//				}
//			}
//			if (sugarStructure == null && !encoding.equalsIgnoreCase("gws")) {
//				//encoding is not supported
//				throw new IllegalArgumentException("Encoding " + encoding + " is not supported");
//			}
//		} else {
//			String structure;
//			if (encoding != null && encoding.equalsIgnoreCase("gws")) { // glycoworkbench encoding
//				structure = new GWSImporter().parse(glycan.getStructure());
//				//logger.debug("converted from gws:  {}", structure);
//			} else {
//				// assume GlycoCT encoding
//				structure = glycan.getStructure();
//			}
//			sugarStructure = StructureParserValidator.parse(structure);
//		}
//		
//		
//		if (StructureParserValidator.isValid(sugarStructure)) {		
//			return sugarStructure;
//		} else {
//			throw new IllegalArgumentException("Validation error, please submit a valid structure");
//		}
//	}
//	
//	private void getCompositions (GlycanExhibit glycan, Set<GlycanComposition> compositions) {
//		for (Iterator iterator = compositions.iterator(); iterator.hasNext();) {
//			GlycanComposition glycanComposition = (GlycanComposition) iterator
//					.next();
//			String compName = glycanComposition.getComposition().getName();
//			int count = glycanComposition.getCount();
//			switch (compName) {
//				case "Fuc": glycan.setNumberOfFuc(count); 
//							break;
//				case "Gal": glycan.setNumberOfGal(count);
//							break;
//				case "GalA" : glycan.setNumberOfGalA(count);
//							break;
//				case "GalN" : glycan.setNumberOfGalN(count);
//							break;
//				case "GalNAc" : glycan.setNumberOfGalNAc(count);
//							break;
//				case "Glc" : glycan.setNumberOfGlc(count);
//							break;
//				case "GlcA" : glycan.setNumberOfGlcA(count);
//							break;
//				case "GlcN" : glycan.setNumberOfGlcN(count);
//							break;
//				case "GlcNAc" : glycan.setNumberOfGlcNAc(count);
//							break;
//				case "Kdn" : glycan.setNumberOfKdn(count);
//							break;
//				case "Man" : glycan.setNumberOfMan(count);
//							break;
//				case "ManA" : glycan.setNumberOfManA(count);
//							break;
//				case "ManN" : glycan.setNumberOfManN(count);
//							break;
//				case "ManNAc" : glycan.setNumberOfManNAc(count);
//							break;
//				case "NeuAc" : glycan.setNumberOfNeuAc(count);
//							break;
//				case "NeuGc" : glycan.setNumberOfNeuGc(count);
//							break;
//				case "Xly" : glycan.setNumberOfXyl(count);
//							break;
//				case "IdoA" : glycan.setNumberOfIdoA(count);
//							break;
//			}
//		}
//	}
	
	@RequestMapping(value = "/add", method = RequestMethod.POST, consumes={"application/xml", "application/json"})
    @ApiOperation(value="Adds a glycan structure to the system, returns the assigned glycan identifier", 
    			response=GlycanResponse.class, notes="Only currently logged in user can submit a structure, the returned object contains the accession number assigned or the already existing one")
    @ApiResponses(value ={@ApiResponse(code=201, message="Structure added successfully"),
    		@ApiResponse(code=400, message="Illegal argument - Glycan should be valid"),
    		@ApiResponse(code=401, message="Unauthorized"),
    		@ApiResponse(code=415, message="Media type is not supported"),
    		@ApiResponse(code=500, message="Internal Server Error")})
	public ResponseEntity<GlycanResponse> submitStructure (
		    @ApiParam(required=true, value="Glycan") 
		    @RequestBody (required=true)
		    @Valid GlycanInput glycan,
		    Principal p) throws Exception {

		String userName = p.getName();
		logger.debug("begin import ParseValidate");
//		Sugar sugarStructure = importParseValidate(glycan);
		logger.debug("end import ParseValidate");
		
		String exportedStructure = null;
		Double mass=null;
		boolean massError = false;
		Exception massException=null;
		
//		if (sugarStructure == null) {
//			throw new IllegalArgumentException("Structure cannot be imported");
//		}
		
		// export into GlycoCT to make sure we have a uniform structure content in the DB
		try {
			logger.debug("begin exportStructure");
//			exportedStructure = StructureParserValidator.exportStructure(sugarStructure);
//			logger.debug("exported Structure: {}", exportedStructure);
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot export into common encoding: " + e.getMessage());
		}
//		try {
//			mass = massCalculator.calculateMass(sugarStructure);
//			if (mass != null && mass <= 0) {
//				// could not calculate mass
//				logger.info("Could not calculate mass for the structure!");
//				mass = null;
//			}
//		} catch (Exception e) {
//			// failed to calculate mass
//			massError = true;
//			massException = e;
//			mass = null;
//		}
		
		GlycanResponse response = null;
		try {
			response = glycanManager.addStructure(exportedStructure, userName, mass);
			if (response.getExisting()) {
				logger.debug("GLYCAN_EXISTS:" + response.getAccessionNumber() + userName);
			} else {
				if (response.getQuotaExceeded()) {
					// send email to the moderator
					List<User> moderators = userManager.getModerators();
					emailManager.sendUserQuotaAlert(moderators, userName);
					throw new UserQuotaExceededException("Cannot add the glycan, user's quota exceeded. Please contact the Administrator");
				}
				logger.info ("GLYCAN_ADD,{},{}" + response.getAccessionNumber() + userName);
			}
		} catch (DataIntegrityViolationException e) {
			// failed to add, need a new accession number
			boolean exception = true;
			do {
				logger.info("Duplicate accession number retrying." +  e.getMessage());
				try {
					response = glycanManager.assignNewAccessionNumber(exportedStructure, userName);
					exception = false;
				} catch (DataIntegrityViolationException ex) {
					exception = true;
				}
			} while (exception);
		} 
		if (massError) {
			logger.info("Mass calculation exception occured for glycan {}. Reason: {}" + response.getAccessionNumber() + massException);
		}

		return new ResponseEntity<GlycanResponse> (response, HttpStatus.CREATED);
	}
	
	@RequestMapping(value="/{accessionNumber}/delete", method = RequestMethod.DELETE)
    @ApiOperation(value="Deletes the glycan with given accession number", response=Confirmation.class, notes="This can be accessed by the Administrator user only")
    @ApiResponses (value ={@ApiResponse(code=200, message="Glycan deleted successfully"), 
    		@ApiResponse(code=401, message="Unauthorized"),
    		@ApiResponse(code=403, message="Not enough privileges to delete glycans"),
    		@ApiResponse(code=404, message="Glycan does not exist"),
    		@ApiResponse(code=500, message="Internal Server Error")})
	public @ResponseBody Confirmation deleteGlycan (@ApiParam(required=true, value="accession number of the glycan to be deleted") @PathVariable("accessionNumber") String accessionNumber) {
		glycanManager.deleteGlycanByAccessionNumber(accessionNumber);
		return new Confirmation("Glycan deleted successfully", HttpStatus.OK.value());
	}
	
	@RequestMapping(value="/delete/list", method = RequestMethod.DELETE, consumes={"application/xml", "application/json"})
    @ApiOperation(value="Deletes the glycans with given accession numbers", response=Confirmation.class, notes="This can be accessed by the Administrator user only")
    @ApiResponses (value ={@ApiResponse(code=200, message="Glycans deleted successfully"), 
    		@ApiResponse(code=400, message="Illegal argument - Only accession numbers should be given"),
    		@ApiResponse(code=401, message="Unauthorized"),
    		@ApiResponse(code=403, message="Not enough privileges to delete glycans"),
    		@ApiResponse(code=404, message="Glycan does not exist"),
    		@ApiResponse(code=415, message="Media type is not supported"),
    		@ApiResponse(code=500, message="Internal Server Error")})
	public @ResponseBody Confirmation deleteGlycans (
			@ApiParam(required=true, value="accession numbers of the glycans to be deleted") 
			@RequestBody(required=true) 
			GlycanList accessionNumbers) {
		GlycanList list = new GlycanList();
		List<Object> objectList = accessionNumbers.getGlycans();
		List<String> numberList = new ArrayList<>();
		for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
			Object item = (Object) iterator.next();
			if (item instanceof String) {
				numberList.add((String)item);
			}
			else {
				throw new IllegalArgumentException("Only accession numbers should be given as input");
			}
		}
		glycanManager.deleteGlycansByAccessionNumber(numberList);
		return new Confirmation("Glycans deleted successfully", HttpStatus.OK.value());
	}
	
	@RequestMapping(value = "/{accessionNumber}", method = RequestMethod.GET, produces={"application/xml", "application/json"})
	@ApiOperation(value="Retrieves glycan by accession number", response=Glycan.class)
	@ApiResponses (value ={@ApiResponse(code=200, message="Success"),
			@ApiResponse(code=404, message="Glycan does not exist"),
			@ApiResponse(code=500, message="Internal Server Error")})
	public @ResponseBody Glycan getGlycan (
			@ApiParam(required=true, value="id of the glycan") @PathVariable("accessionNumber") String accessionNumber) {
		Glycan glycanEntity = glycanManager.getGlycanByAccessionNumber(accessionNumber);
		glycanEntity.getContributor().setEmail(""); // hide the email
		return glycanEntity;
	}
	
//	@RequestMapping(value = "/{accessionNumber}/rdf", method = RequestMethod.GET, produces={"text/turtle"})
//	@ApiOperation(value="Retrieves glycan RDF by accession number", response=ResponseEntity.class)
//	@ApiResponses (value ={@ApiResponse(code=200, message="Success"),
//			@ApiResponse(code=404, message="Glycan does not exist"),
//			@ApiResponse(code=500, message="Internal Server Error")})
//	public ResponseEntity<String> getGlycanRDF (
//			@ApiParam(required=true, value="id of the glycan") 
//			@PathVariable("accessionNumber") String accessionNumber) throws IOException {
//		
//		Glycan glycanEntity = glycanManager.getGlycanByAccessionNumber(accessionNumber);
//		glycanEntity.getContributor().setEmail(""); // hide the email
//		
//		GlycanStructureProvider strProvider = new GlycanStructureProvider();
//		strProvider.setGlycan(glycanEntity);
//		
//		// generate RDF export
//        RDFGeneratorGlycanConfig t_config = createRDFConfiguration();
//        ResidueTranslator t_residueTransTranslation = new ResidueTranslator();
//        GlycoRDFWriter t_writer = new GlycoRDFWriter(strProvider, t_config, t_residueTransTranslation);
//        t_writer.setNamespace ("glytoucan");
//        t_writer.write(glycanEntity.getGlycanId());
//        StringWriter t_writerString = new StringWriter();
//        t_writer.serialze(t_writerString, "TURTLE");
//        t_writerString.flush();
//        
//        // write it out
//    	return new ResponseEntity<String>(t_writerString.toString(), HttpStatus.OK);
//	}
	
	@RequestMapping(value="/list/accessionNumber", method = RequestMethod.POST, consumes={"application/xml", "application/json"}, produces={"application/xml", "application/json"})
    @ApiOperation(value="Retrieves the glycans with given accession numbers", response=GlycanList.class, notes="optional payload parameter allows to get the 'full' glycan object details (default) or 'exhibit' format conforming to SIMILE Exhibit")
    @ApiResponses (value ={@ApiResponse(code=200, message="Glycans retrieved successfully"), 
    		@ApiResponse(code=400, message="Illegal argument - Only accession numbers should be given"),
    		@ApiResponse(code=404, message="Glycan does not exist"),
    		@ApiResponse(code=415, message="Media type is not supported"),
    		@ApiResponse(code=500, message="Internal Server Error")})
	public @ResponseBody GlycanList listGlycansByAccessionNumbers (
			@ApiParam(required=true, value="accession numbers of the glycans to be retrieved") 
			@RequestBody
			GlycanList accessionNumbers,
			@ApiParam(required=false, value="payload: full (default)") 
			@RequestParam(required=false, value="payload", defaultValue="full")
			String payload) {
		
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String imageURL;
		String requestURI = request.getRequestURL().toString();
		
		GlycanList list = new GlycanList();
		List<Object> objectList = accessionNumbers.getGlycans();
		List<String> numberList = new ArrayList<>();
		for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
			Object item = (Object) iterator.next();
			if (item instanceof String) {
				numberList.add((String)item);
			}
			else {
				throw new IllegalArgumentException("Only accession numbers should be given");
			}
		}
		List<Glycan> glycans = glycanManager.getGlycansByAccessionNumbers(numberList);
		for (Iterator<Glycan> iterator = glycans.iterator(); iterator.hasNext();) {
			Glycan glycanEntity = (Glycan) iterator.next();
			glycanEntity.getContributor().setEmail(""); // hide email from any user
		}
		list.setGlycans(glycans.toArray());
		return list;
	}
	
	@RequestMapping(value = "/list", method = RequestMethod.GET, produces={"application/xml", "application/json"})
	@ApiOperation (value="Lists all the glycans", response=GlycanList.class, notes="payload option can be omitted to get only the glycan ids or set to 'full' to get glycan objects. 'exhibit' option allows to get glycan objects conforming to SIMILE Exhibit Json"
			+ " format.")
	@ApiResponses (value ={@ApiResponse(code=200, message="Success"), 
			@ApiResponse(code=500, message="Internal Server Error")})
	public @ResponseBody GlycanList listGlycans (
			@ApiParam(required=false, value="payload: id (default) or full or exhibit") 
			@RequestParam(required=false, value="payload", defaultValue="id")
			String payload) {
		GlycanList list = new GlycanList();
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String imageURL;
		String requestURI = request.getRequestURL().toString();
		if (payload != null && (payload.equalsIgnoreCase("full") || payload.equalsIgnoreCase("exhibit"))) {
			List<Glycan> glycans = glycanManager.getGlycans();
			for (Iterator<Glycan> iterator = glycans.iterator(); iterator.hasNext();) {
				Glycan glycanEntity = (Glycan) iterator.next();
				glycanEntity.getContributor().setEmail(""); // hide email from any user
			}
			list.setGlycans(glycans.toArray());
		}
		else {
			list.setGlycans(glycanManager.getGlycanIds().toArray());
		}
		return list;
	}
	
	@RequestMapping(value = "/search/substructure", method = RequestMethod.GET, produces={"application/json"})
    @ApiOperation(value="Searches for glycan structures containing the given structure, returns the existing glycan identifiers if any found", 
    			response=GlycanList.class)
    @ApiResponses(value ={@ApiResponse(code=200, message="Found match(es)"), 
    				@ApiResponse(code=400, message="Illegal argument - Glycan should be valid"),
    				@ApiResponse(code=404, message="No matching glycan is found"),
    				@ApiResponse(code=415, message="Media type is not supported"),
    				@ApiResponse(code=500, message="Internal Server Error")})
	public @ResponseBody GlycanList substructureSearch (
			@RequestBody (required=true)
			@ApiParam(required=true, value="Glycan") 
			@Valid
			GlycanInput glycan,
			@ApiParam(required=false, value="payload: id (default) or full") 
			@RequestParam(required=false, value="payload", defaultValue="id")
			String payload) throws Exception {
		logger.debug("Substructure search");
		
//		Sugar sugarStructure = importParseValidate(glycan);
//		if (sugarStructure == null) {
//			throw new IllegalArgumentException("Structure cannot be imported");
//		}
		String exportedStructure;
//		try {
//			exportedStructure = StructureParserValidator.exportStructure(sugarStructure);
//		} catch (Exception e) {
//			throw new IllegalArgumentException("Cannot export into common encoding: " + e.getMessage());
//		}
		
		GlycanList matches = new GlycanList();
//		matches.setGlycans(glycanManager.subStructureSearch(exportedStructure).toArray());
		if (payload != null && (payload.equalsIgnoreCase("full"))) {
			matches = listGlycansByAccessionNumbers(matches, payload);
		}
		return matches;
	}
	
//	@RequestMapping(value = "/search/composition", method = RequestMethod.POST, consumes={"application/xml", "application/json"}, produces={"application/xml", "application/json"})
//    @ApiOperation(value="Searches for glycan structures with the given composition", 
//    			response=GlycanList.class)
//    @ApiResponses(value ={@ApiResponse(code=200, message="Found match(es)"), 
//    				@ApiResponse(code=400, message="Illegal argument - Search Criteria should be valid"),
//    				@ApiResponse(code=404, message="No matching glycan is found"),
//    				@ApiResponse(code=415, message="Media type is not supported"),
//    				@ApiResponse(code=500, message="Internal Server Error")})
//	public @ResponseBody GlycanList compositionSearch (
//			@RequestBody (required=true)
//			@ApiParam(required=true, value="Search Criteria")
//			@Valid CompositionSearchInput input,
//			@ApiParam(required=false, value="payload: id (default) or full or exhibit") 
//			@RequestParam(required=false, value="payload", defaultValue="id")
//			String payload) throws SugarImporterException, GlycoVisitorException, SearchEngineException {
//		GlycanList matches = new GlycanList();
//		matches.setGlycans((glycanManager.compositionSearch(input)).toArray());
//		if (payload != null && (payload.equalsIgnoreCase("exhibit") || payload.equalsIgnoreCase("full"))) {
//			matches = listGlycansByAccessionNumbers(matches, payload);
//		}
//		return matches;
//	}
	
	@RequestMapping(value = "/search/user", method = RequestMethod.POST, consumes={"application/xml", "application/json"}, produces={"application/xml", "application/json"})
    @ApiOperation(value="Searches for glycan structures submitted by the given user", notes="You can provide any of the following fields: login name, full name, affiliation and email for the user (password field will be ignored). "
    		+ "The user matching all of the given fields (if any) will be used as the contributor",
    			response=GlycanList.class)
    @ApiResponses(value ={@ApiResponse(code=200, message="Found match(es)"), 
    				@ApiResponse(code=400, message="Illegal argument - Search Criteria should be valid"),
    				@ApiResponse(code=404, message="No matching glycan is found / No user is found matching the criteria"),
    				@ApiResponse(code=415, message="Media type is not supported"),
    				@ApiResponse(code=500, message="Internal Server Error")})
	public @ResponseBody GlycanList getGlycansByContributor (
			@RequestBody 
			@ApiParam(required=true, value="User Criteria")
		    UserInput user,
		    @ApiParam(required=false, value="payload: id (default) or full or exhibit") 
			@RequestParam(required=false, value="payload", defaultValue="id")
			String payload) {
		GlycanList matches = new GlycanList();
		if (user == null || 
			(user.getLoginId() == null && 
			user.getAffiliation() == null && 
			user.getLoginId() == null && 
			user.getEmail() == null)) {
			// invalid input
			throw new IllegalArgumentException("Invalid input: You should at least provide one field of the user");
		} else if ((user.getLoginId() == null || (user.getLoginId() != null && user.getLoginId().isEmpty())) && 
				(user.getAffiliation() == null || (user.getAffiliation() != null && user.getAffiliation().isEmpty())) && 
				(user.getEmail() == null || (user.getEmail() != null && user.getEmail().isEmpty())) &&
				(user.getFullName() == null || (user.getFullName() != null && user.getFullName().isEmpty()) ) ) {
			// invalid input
			throw new IllegalArgumentException("Invalid input: You should at least provide one field of the user");		
		}
		matches.setGlycans(glycanManager.getGlycansByContributor(user).toArray());
		if (payload != null && (payload.equalsIgnoreCase("exhibit") || payload.equalsIgnoreCase("full"))) {
			matches = listGlycansByAccessionNumbers(matches, payload);
		}
		return matches;
	}
	
	@RequestMapping(value = "/search/user/pending", method = RequestMethod.POST, consumes={"application/xml", "application/json"}, produces={"application/xml", "application/json"})
    @ApiOperation(value="Searches for all pending glycan structures submitted by the given user", notes="Administrator/Moderator use only. You can provide any of the following fields: login name, full name, affiliation and email for the user (password field will be ignored). "
    		+ "The user matching all of the given fields (if any) will be used as the contributor",
    			response=GlycanList.class)
    @ApiResponses(value ={@ApiResponse(code=200, message="Found match(es)"), 
    				@ApiResponse(code=400, message="Illegal argument - Search Criteria should be valid"),
    				@ApiResponse(code=401, message="Unauthorized"),
    	    		@ApiResponse(code=403, message="Not enough privileges to delete glycans"),
    				@ApiResponse(code=404, message="No matching glycan is found / No user is found matching the criteria"),
    				@ApiResponse(code=415, message="Media type is not supported"),
    				@ApiResponse(code=500, message="Internal Server Error")})
	public @ResponseBody GlycanList getPendingGlycansByContributor (
			@RequestBody 
			@ApiParam(required=true, value="User Criteria")
		    UserInput user, 
		    @ApiParam(required=false, value="payload: id (default) or full or exhibit") 
			@RequestParam(required=false, value="payload", defaultValue="id")
			String payload) {
		GlycanList matches = new GlycanList();
		if (user == null || 
			(user.getLoginId() == null && 
			user.getAffiliation() == null && 
			user.getLoginId() == null && 
			user.getEmail() == null)) {
			// invalid input
			throw new IllegalArgumentException("Invalid input: You should at least provide one field of the user");
		} else if ((user.getLoginId() == null || (user.getLoginId() != null && user.getLoginId().isEmpty())) && 
				(user.getAffiliation() == null || (user.getAffiliation() != null && user.getAffiliation().isEmpty())) && 
				(user.getEmail() == null || (user.getEmail() != null && user.getEmail().isEmpty())) &&
				(user.getFullName() == null || (user.getFullName() != null && user.getFullName().isEmpty()) ) ) {
			// invalid input
			throw new IllegalArgumentException("Invalid input: You should at least provide one field of the user");		
		}
		matches.setGlycans(glycanManager.getAllPendingGlycansByContributor(user).toArray());
		if (payload != null && (payload.equalsIgnoreCase("exhibit") || payload.equalsIgnoreCase("full"))) {
			matches = listGlycansByAccessionNumbers(matches, payload);
		}
		return matches;
	}
	
	@RequestMapping(value = "/search/motif", method = RequestMethod.POST, produces={"application/xml", "application/json"})
    @ApiOperation(value="Searches for glycan structures containing the given motif", 
    			response=GlycanList.class)
    @ApiResponses(value ={@ApiResponse(code=200, message="Found match(es)"), 
    				@ApiResponse(code=400, message="Illegal argument - Motif name should be valid"),
    				@ApiResponse(code=404, message="No matching glycan is found / No motif is found with given name"),
    				@ApiResponse(code=415, message="Media type is not supported"),
    				@ApiResponse(code=500, message="Internal Server Error")})
	public @ResponseBody GlycanList getGlycansByMotif (
			@RequestBody 
			@ApiParam(required=true, value="Motif Name")
		    String motifName, 
		    @ApiParam(required=false, value="payload: id (default) or full or exhibit") 
			@RequestParam(required=false, value="payload", defaultValue="id")
			String payload) throws Exception {
		GlycanList matches = new GlycanList();
		if (motifName == null || motifName.isEmpty())
			// invalid input
			throw new IllegalArgumentException("Invalid input: You have to provide the name of the motif");
		matches.setGlycans(glycanManager.motifSearch(motifName).toArray());
		if (payload != null && (payload.equalsIgnoreCase("exhibit") || payload.equalsIgnoreCase("full"))) {
			matches = listGlycansByAccessionNumbers(matches, payload);
		}
		return matches;
	}
	
	@RequestMapping(value = "/search/exact", method = RequestMethod.POST, consumes={"application/xml", "application/json"}, produces={"application/xml", "application/json"})
    @ApiOperation(value="Searches for a glycan structure having exactly the same structure as the input structure", 
    			response=Glycan.class)
    @ApiResponses(value ={@ApiResponse(code=200, message="Found a match"), 
    				@ApiResponse(code=400, message="Illegal argument - Glycan should be valid"),
    				@ApiResponse(code=404, message="No matching glycan is found"),
    				@ApiResponse(code=415, message="Media type is not supported"),
    				@ApiResponse(code=500, message="Internal Server Error")})
	public @ResponseBody Glycan exactStructureSearch (
			@RequestBody (required=true)
			@ApiParam(required=true, value="Glycan") 
			@Valid
			GlycanInput glycan) throws Exception {
		
//		String encoding = glycan.getEncoding();
//		Sugar sugarStructure = importParseValidate(glycan);
//		if (sugarStructure == null) {
//			throw new IllegalArgumentException("Structure cannot be imported");
//		}
		String exportedStructure = null;
//		
//		// export into GlycoCT to make sure we have a uniform structure content in the DB
//		try {
//			exportedStructure = StructureParserValidator.exportStructure(sugarStructure);
//		} catch (Exception e) {
//			throw new IllegalArgumentException("Cannot export into common encoding: " + e.getMessage());
//		}
			
		Glycan glycanEntity = glycanManager.getGlycanByStructure(exportedStructure);
		glycanEntity.getContributor().setEmail(""); // hide the email
		return glycanEntity;
	}
	
//	@RequestMapping (value="/search/complex", method=RequestMethod.POST, consumes={"application/xml", "application/json"}, produces={"application/xml", "application/json"})
//    @ApiOperation(value="Searches for glycan structures using a combination of other searches with union/intersection/difference",
//        response=GlycanList.class)
//            @ApiResponses(value ={@ApiResponse(code=200, message="Found match(es)"),
//            @ApiResponse(code=400, message="Illegal argument - Search input should be valid"),
//            @ApiResponse(code=415, message="Media type is not supported"),
//            @ApiResponse(code=500, message="Internal Server Error")})
//    public @ResponseBody GlycanList complexSearch (
//            @ApiParam (required=true, value="search input")
//            @RequestBody
//            CombinationSearch search, 
//            @ApiParam(required=false, value="payload: id (default) or full or exhibit") 
//			@RequestParam(required=false, value="payload", defaultValue="id")
//			String payload) throws Exception {
//        GlycanList matches = new GlycanList();
//        matches.setGlycans(search.search(glycanManager).toArray());
//        if (payload != null && (payload.equalsIgnoreCase("exhibit") || payload.equalsIgnoreCase("full"))) {
//			matches = listGlycansByAccessionNumbers(matches, payload);
//		}
//        return matches;
//    }

	@RequestMapping(value="/add/list", method=RequestMethod.POST, consumes={"application/xml", "application/json"}, produces={"application/xml", "application/json"}) 
	@ApiOperation(value="Add multiple glycan structures submitted as a list", response=GlycanResponseList.class)
	@ApiResponses(value ={@ApiResponse(code=201, message="Glycans added successfully"), 
			@ApiResponse(code=400, message="Illegal argument - Glycan List should be valid"),
			@ApiResponse(code=401, message="Unauthorized. Only the logged in user can submit structures"),
			@ApiResponse(code=415, message="Media type is not supported"),
			@ApiResponse(code=500, message="Internal Server Error")})
	public @ResponseBody GlycanResponseList glycanListAdd (
			@ApiParam(name="glycans", required=true)
			@RequestBody GlycanInputList glycans, Principal p) {
		if (glycans == null || glycans.getGlycans().isEmpty()) {
			throw new IllegalArgumentException("Invalid Input: The list is empty - No glycans to add");
		}
		List<GlycanResponse> responseList = new ArrayList<>();
		List<GlycanErrorResponse> errorList = new ArrayList<>();
		for (Iterator<?> iterator = glycans.getGlycans().iterator(); iterator.hasNext();) {
			GlycanInput glycan = (GlycanInput) iterator.next();
			try {
				ResponseEntity<GlycanResponse> resp = submitStructure(glycan, p);
				GlycanResponse gRes = resp.getBody();
				gRes.setStructure(glycan.getStructure());
				responseList.add(gRes);
			} catch (UserQuotaExceededException e) {
				throw e;
			} catch (IllegalArgumentException e) {
				GlycanErrorResponse error = new GlycanErrorResponse();
				error.setErrorMessage(e.getMessage());
				error.setStructure(glycan.getStructure());
				error.setStatusCode(HttpStatus.BAD_REQUEST.value());
				error.setErrorCode(ErrorCodes.INVALID_INPUT);
				errorList.add(error);
				logger.warn("Failed to add a glycan from the list. Reason: {}"+ e.getMessage());
			} catch (SugarImporterException e) {
				GlycanErrorResponse error = new GlycanErrorResponse();
				error.setErrorMessage("Failed to import the structure from encoding " + glycan.getFormat() + ". Reason: " + e.getErrorText());
				error.setStructure(glycan.getStructure());
				error.setStatusCode(HttpStatus.BAD_REQUEST.value());
				error.setErrorCode(ErrorCodes.PARSE_ERROR);
				errorList.add(error);
				logger.warn("Failed to import the structure from encoding " + glycan.getFormat() + ". Reason: " + e.getErrorText());
			} catch (GlycoVisitorException e) {
				GlycanErrorResponse error = new GlycanErrorResponse();
				error.setErrorMessage("Failed to validate the structure. Reason: " + e.getMessage());
				error.setStructure(glycan.getStructure());
				error.setStatusCode(HttpStatus.BAD_REQUEST.value());
				error.setErrorCode(ErrorCodes.INVALID_STRUCTURE);
				errorList.add(error);
				logger.warn("Failed to validate the structure. Reason: " + e.getMessage());
			} catch (Exception e) {
				GlycanErrorResponse error = new GlycanErrorResponse();
				error.setErrorMessage("Failed to add the structure. Reason: " + e.getMessage());
				error.setStructure(glycan.getStructure());
				error.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				error.setErrorCode(ErrorCodes.INTERNAL_ERROR);
				errorList.add(error);
				logger.error("Failed to add the structure. Reason: " + e.getMessage());
			}
		}
		
		return new GlycanResponseList(responseList, errorList);
	}
	
	@RequestMapping(value="/check/list", method=RequestMethod.POST, consumes={"application/xml", "application/json"}, produces={"application/xml", "application/json"}) 
	@ApiOperation(value="Check multiple glycan structures to see if they already exist in the registry and whether they are valid to be added", response=GlycanResponseList.class)
	@ApiResponses(value ={@ApiResponse(code=200, message="Glycans checked successfully"), 
			@ApiResponse(code=400, message="Illegal argument - Glycan List should be valid"),
			@ApiResponse(code=415, message="Media type is not supported"),
			@ApiResponse(code=500, message="Internal Server Error")})
	public @ResponseBody GlycanResponseList glycanListCheck (
			@ApiParam(name="glycans", required=true)
			@RequestBody GlycanInputList glycans) {
		if (glycans == null || glycans.getGlycans().isEmpty()) {
			throw new IllegalArgumentException("Invalid Input: The list is empty - No glycans to check");
		}
		List<GlycanResponse> responseList = new ArrayList<>();
		List<GlycanErrorResponse> errorList = new ArrayList<>();
		for (Iterator<?> iterator = glycans.getGlycans().iterator(); iterator.hasNext();) {
			GlycanInput glycan = (GlycanInput) iterator.next();
			GlycanResponse gRes = new GlycanResponse();
			gRes.setStructure(glycan.getStructure());
			try {
				Glycan glycanEntity = exactStructureSearch(glycan);
				if (glycanEntity != null) {
					gRes.setExisting(true);
					gRes.setPending(glycanManager.isPending(glycanEntity));
					gRes.setAccessionNumber(glycanEntity.getAccessionNumber());
				}
				else
					gRes.setExisting(false);
				responseList.add(gRes);
			} catch (Exception e) {
				if (e instanceof GlycanNotFoundException) {
					// new glycan
					gRes.setExisting(false);
					responseList.add(gRes);
				}
				else {
					GlycanErrorResponse error = new GlycanErrorResponse();
					if (e instanceof SugarImporterException) {
						error.setErrorMessage("Failed to import the structure. Reason: " + ((SugarImporterException)e).getErrorText());
						error.setErrorCode(ErrorCodes.PARSE_ERROR);
						logger.info("Failed to import the structure. Reason: {}"+ ((SugarImporterException)e).getErrorText());
					} else if (e instanceof UserQuotaExceededException) {
						error.setErrorMessage("User quota exceeded");
						error.setErrorCode (ErrorCodes.NOT_ALLOWED);
						logger.error("User quota exceeded");
					}
					else {
						error.setErrorMessage("Failed to validate the structure. Reason: " + e.getMessage());
						error.setErrorCode(ErrorCodes.INVALID_STRUCTURE);
						logger.info("Failed to validate the structure. Reason: {}"+ e.getMessage());
					}
					error.setStructure(glycan.getStructure());
					error.setStatusCode(HttpStatus.BAD_REQUEST.value());
					errorList.add(error);
				}
			}
		}
		return new GlycanResponseList(responseList, errorList);
	}
	
	/**
     * Accept a file containing glycan structures
	 * @throws IOException 
     */
    @RequestMapping(value = "/add/batchFile", method = RequestMethod.POST)
    @ApiOperation(value="Add glycan structures submitted in a file.", response=Confirmation.class)
    @ApiResponses(value ={@ApiResponse(code=201, message="Glycans added successfully"), 
			@ApiResponse(code=400, message="Illegal argument - File should contain valid content"),
			@ApiResponse(code=401, message="Unauthorized. Only the logged in user can submit structures"),
			@ApiResponse(code=415, message="Media type is not supported"),
			@ApiResponse(code=500, message="Internal Server Error")})
    public @ResponseBody GlycanResponseList batchGlycanSubmit( 
    		@ApiParam (name="encoding", defaultValue="glycoct")
    		@RequestParam(value="encoding", defaultValue="glycoct") String encoding,
    		@ApiParam(required=true, name="file")
            @RequestParam("file") MultipartFile glycanFile, Principal p) throws IOException {
 
        if (glycanFile != null && !glycanFile.isEmpty()) {
        	byte[] bytes = glycanFile.getBytes();
        	InputStream stream = glycanFile.getInputStream();
        	BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        	
        	List<GlycanInput> list = new ArrayList<GlycanInput>();
        	if (encoding.equalsIgnoreCase("glycoct_xml") || encoding.equalsIgnoreCase("cabosML") || encoding.equalsIgnoreCase("glyde")) {
        		// xml - parse xml, call appropriate importer for each children
        		throw new IllegalArgumentException("The encoding is not supported yet!");
        	} else if (encoding.equalsIgnoreCase("carbbank")) {
        		// separator is "empty line" ?
        		throw new IllegalArgumentException("The encoding is not supported yet!");
        	} else if (encoding.equalsIgnoreCase("glycoct") || encoding.equalsIgnoreCase("kcf")) {
        		// separator is ///
        		String line = null;
        		String structure ="";
        		while ((line = reader.readLine()) != null) {
        			if (line.equalsIgnoreCase("///")) {
        				// add the structure
        				if (!structure.isEmpty()) {
        					// submit each structure
        	        		GlycanInput glycan = new GlycanInput();
        	        		glycan.setFormat(encoding);
        	        		glycan.setStructure(structure.trim());
        	        		list.add(glycan);
        				}
        				//new structure
        				structure= "";
        			}
        			else {
        				structure += line + "\n";
        			}
        		}
        		// EOF check
				if (!structure.isEmpty()) {
					// submit each structure
	        		GlycanInput glycan = new GlycanInput();
	        		glycan.setFormat(encoding);
	        		glycan.setStructure(structure.trim());
	        		list.add(glycan);
				}
        	}
        	else { // each line corresponds to a glycan structure
	        	String structure = null;
	        	while ((structure = reader.readLine()) != null) {
	        		logger.debug("Line: " + structure);
	        		// submit each structure
	        		GlycanInput glycan = new GlycanInput();
	        		glycan.setFormat(encoding);
	        		glycan.setStructure(structure);
	        		list.add(glycan);
	        	}
        	}
        	GlycanInputList inputList = new GlycanInputList();
        	inputList.setGlycans(list);
        	return glycanListAdd (inputList, p);
        } else {
        	throw new IllegalArgumentException("Invalid input: The file is empty - No glycans to add");
        }
    }
    
    @RequestMapping(value="/{accessionNumber}/image", method=RequestMethod.GET, produces={MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.IMAGE_JPEG_VALUE})
    @ApiOperation(value="Retrieves glycan image by accession number", response=Byte[].class)
	@ApiResponses (value ={@ApiResponse(code=200, message="Success"),
			@ApiResponse(code=400, message="Illegal argument"),
			@ApiResponse(code=404, message="Glycan does not exist"),
			@ApiResponse(code=500, message="Internal Server Error")})
    public @ResponseBody ResponseEntity<byte[]> getGlycanImage (
    		@ApiParam(required=true, value="id of the glycan") 
    		@PathVariable("accessionNumber") 
    		String accessionNumber,
    		@ApiParam(required=false, value="format of the the glycan image", defaultValue="png") 
    		@RequestParam("format") 
    		String format,
    		@ApiParam(required=false, value="notation to use to generate the image", defaultValue="cfg") 
    		@RequestParam("notation") 
    		String notation,
    		@ApiParam(required=false, value="style of the image", defaultValue="compact") 
    		@RequestParam("style") 
    		String style
    		) throws Exception {
    	Glycan glycanEntity = glycanManager.getGlycanByAccessionNumber(accessionNumber);
    	
    	byte[] bytes = null;
//    			imageGenerator.getImage(glycanEntity.getStructure(), format, notation, style);

				
		HttpHeaders headers = new HttpHeaders();
    	if (format == null || format.equalsIgnoreCase("png")) {    
		    headers.setContentType(MediaType.IMAGE_PNG);
    	} else if (format.equalsIgnoreCase("svg")) {
		    headers.setContentType(MediaType.APPLICATION_XML);
    	} else if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) {
    		headers.setContentType(MediaType.IMAGE_JPEG);
    	}
		return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
    }
    
    @RequestMapping(value="/image/glycan", method=RequestMethod.POST, consumes={"application/xml", "application/json"}, produces={MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.IMAGE_JPEG_VALUE})
    @ApiOperation(value="Retrieves glycan image by accession number", response=Byte[].class)
	@ApiResponses (value ={@ApiResponse(code=200, message="Success"),
			@ApiResponse(code=400, message="Illegal argument"),
			@ApiResponse(code=404, message="Glycan does not exist"),
			@ApiResponse(code=415, message="Media type is not supported"),
			@ApiResponse(code=500, message="Internal Server Error")})
    public @ResponseBody ResponseEntity<byte[]> getGlycanImageByStructure (
    		@RequestBody (required=true)
			@ApiParam(required=true, value="Glycan") 
			@Valid
			GlycanInput glycan,
    		@ApiParam(required=false, value="format of the the glycan image", defaultValue="png") 
    		@RequestParam("format") 
    		String format,
    		@ApiParam(required=false, value="notation to use to generate the image", defaultValue="cfg") 
    		@RequestParam("notation") 
    		String notation,
    		@ApiParam(required=false, value="style of the image", defaultValue="compact") 
    		@RequestParam("style") 
    		String style
    		) throws Exception {
    	
//    	Sugar sugarStructure = importParseValidate(glycan);
//    	if (sugarStructure == null) {
//			throw new IllegalArgumentException("Structure cannot be imported");
//		}
		String exportedStructure;
//		
//		// export into GlycoCT
//		try {
//			exportedStructure = StructureParserValidator.exportStructure(sugarStructure);
//		} catch (Exception e) {
//			throw new IllegalArgumentException("Cannot export into common encoding: " + e.getMessage());
//		}
    	
    	byte[] bytes = null;
//		imageGenerator.getImage(exportedStructure, format, notation, style);
		
		HttpHeaders headers = new HttpHeaders();
    	if (format == null || format.equalsIgnoreCase("png")) {    
		    headers.setContentType(MediaType.IMAGE_PNG);
    	} else if (format.equalsIgnoreCase("svg")) {
		    headers.setContentType(MediaType.APPLICATION_XML);
    	} else if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) {
    		headers.setContentType(MediaType.IMAGE_JPEG);
    	}
		return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
    }
}