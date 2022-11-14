package com.isoft.rfid.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.isoft.rfid.IntegrationTest;
import com.isoft.rfid.domain.Images;
import com.isoft.rfid.repository.ImagesRepository;
import com.isoft.rfid.repository.search.ImagesSearchRepository;
import com.isoft.rfid.service.dto.ImagesDTO;
import com.isoft.rfid.service.mapper.ImagesMapper;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.apache.commons.collections4.IterableUtils;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Base64Utils;

/**
 * Integration tests for the {@link ImagesResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ImagesResourceIT {

    private static final String DEFAULT_GUID = "AAAAAAAAAA";
    private static final String UPDATED_GUID = "BBBBBBBBBB";

    private static final String DEFAULT_PLATE = "AAAAAAAAAA";
    private static final String UPDATED_PLATE = "BBBBBBBBBB";

    private static final byte[] DEFAULT_IMAGE_LP = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_IMAGE_LP = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_IMAGE_LP_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_IMAGE_LP_CONTENT_TYPE = "image/png";

    private static final byte[] DEFAULT_IMAGE_THUMB = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_IMAGE_THUMB = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_IMAGE_THUMB_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_IMAGE_THUMB_CONTENT_TYPE = "image/png";

    private static final String DEFAULT_ANPR = "AAAAAAAAAA";
    private static final String UPDATED_ANPR = "BBBBBBBBBB";

    private static final String DEFAULT_RFID = "AAAAAAAAAA";
    private static final String UPDATED_RFID = "BBBBBBBBBB";

    private static final String DEFAULT_DATA_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_DATA_STATUS = "BBBBBBBBBB";

    private static final Long DEFAULT_GANTRY = 1L;
    private static final Long UPDATED_GANTRY = 2L;

    private static final Long DEFAULT_LANE = 1L;
    private static final Long UPDATED_LANE = 2L;

    private static final Long DEFAULT_KPH = 1L;
    private static final Long UPDATED_KPH = 2L;

    private static final Long DEFAULT_AMBUSH = 1L;
    private static final Long UPDATED_AMBUSH = 2L;

    private static final Long DEFAULT_DIRECTION = 1L;
    private static final Long UPDATED_DIRECTION = 2L;

    private static final Long DEFAULT_VEHICLE = 1L;
    private static final Long UPDATED_VEHICLE = 2L;

    private static final String DEFAULT_ISSUE = "AAAAAAAAAA";
    private static final String UPDATED_ISSUE = "BBBBBBBBBB";

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/images";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/images";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private ImagesMapper imagesMapper;

    @Autowired
    private ImagesSearchRepository imagesSearchRepository;

    @Autowired
    private MockMvc restImagesMockMvc;

    private Images images;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Images createEntity() {
        Images images = new Images()
            .guid(DEFAULT_GUID)
            .plate(DEFAULT_PLATE)
            .imageLp(DEFAULT_IMAGE_LP)
            .imageLpContentType(DEFAULT_IMAGE_LP_CONTENT_TYPE)
            .imageThumb(DEFAULT_IMAGE_THUMB)
            .imageThumbContentType(DEFAULT_IMAGE_THUMB_CONTENT_TYPE)
            .anpr(DEFAULT_ANPR)
            .rfid(DEFAULT_RFID)
            .dataStatus(DEFAULT_DATA_STATUS)
            .gantry(DEFAULT_GANTRY)
            .lane(DEFAULT_LANE)
            .kph(DEFAULT_KPH)
            .ambush(DEFAULT_AMBUSH)
            .direction(DEFAULT_DIRECTION)
            .vehicle(DEFAULT_VEHICLE)
            .issue(DEFAULT_ISSUE)
            .status(DEFAULT_STATUS);
        return images;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Images createUpdatedEntity() {
        Images images = new Images()
            .guid(UPDATED_GUID)
            .plate(UPDATED_PLATE)
            .imageLp(UPDATED_IMAGE_LP)
            .imageLpContentType(UPDATED_IMAGE_LP_CONTENT_TYPE)
            .imageThumb(UPDATED_IMAGE_THUMB)
            .imageThumbContentType(UPDATED_IMAGE_THUMB_CONTENT_TYPE)
            .anpr(UPDATED_ANPR)
            .rfid(UPDATED_RFID)
            .dataStatus(UPDATED_DATA_STATUS)
            .gantry(UPDATED_GANTRY)
            .lane(UPDATED_LANE)
            .kph(UPDATED_KPH)
            .ambush(UPDATED_AMBUSH)
            .direction(UPDATED_DIRECTION)
            .vehicle(UPDATED_VEHICLE)
            .issue(UPDATED_ISSUE)
            .status(UPDATED_STATUS);
        return images;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        imagesSearchRepository.deleteAll();
        assertThat(imagesSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        images = createEntity();
    }

    @Test
    void createImages() throws Exception {
        int databaseSizeBeforeCreate = imagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        // Create the Images
        ImagesDTO imagesDTO = imagesMapper.toDto(images);
        restImagesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(imagesDTO)))
            .andExpect(status().isCreated());

        // Validate the Images in the database
        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(imagesSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        Images testImages = imagesList.get(imagesList.size() - 1);
        assertThat(testImages.getGuid()).isEqualTo(DEFAULT_GUID);
        assertThat(testImages.getPlate()).isEqualTo(DEFAULT_PLATE);
        assertThat(testImages.getImageLp()).isEqualTo(DEFAULT_IMAGE_LP);
        assertThat(testImages.getImageLpContentType()).isEqualTo(DEFAULT_IMAGE_LP_CONTENT_TYPE);
        assertThat(testImages.getImageThumb()).isEqualTo(DEFAULT_IMAGE_THUMB);
        assertThat(testImages.getImageThumbContentType()).isEqualTo(DEFAULT_IMAGE_THUMB_CONTENT_TYPE);
        assertThat(testImages.getAnpr()).isEqualTo(DEFAULT_ANPR);
        assertThat(testImages.getRfid()).isEqualTo(DEFAULT_RFID);
        assertThat(testImages.getDataStatus()).isEqualTo(DEFAULT_DATA_STATUS);
        assertThat(testImages.getGantry()).isEqualTo(DEFAULT_GANTRY);
        assertThat(testImages.getLane()).isEqualTo(DEFAULT_LANE);
        assertThat(testImages.getKph()).isEqualTo(DEFAULT_KPH);
        assertThat(testImages.getAmbush()).isEqualTo(DEFAULT_AMBUSH);
        assertThat(testImages.getDirection()).isEqualTo(DEFAULT_DIRECTION);
        assertThat(testImages.getVehicle()).isEqualTo(DEFAULT_VEHICLE);
        assertThat(testImages.getIssue()).isEqualTo(DEFAULT_ISSUE);
        assertThat(testImages.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    void createImagesWithExistingId() throws Exception {
        // Create the Images with an existing ID
        images.setId(1L);
        ImagesDTO imagesDTO = imagesMapper.toDto(images);

        int databaseSizeBeforeCreate = imagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(imagesSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restImagesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(imagesDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Images in the database
        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkGuidIsRequired() throws Exception {
        int databaseSizeBeforeTest = imagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        // set the field null
        images.setGuid(null);

        // Create the Images, which fails.
        ImagesDTO imagesDTO = imagesMapper.toDto(images);

        restImagesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(imagesDTO)))
            .andExpect(status().isBadRequest());

        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkDataStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = imagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        // set the field null
        images.setDataStatus(null);

        // Create the Images, which fails.
        ImagesDTO imagesDTO = imagesMapper.toDto(images);

        restImagesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(imagesDTO)))
            .andExpect(status().isBadRequest());

        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkGantryIsRequired() throws Exception {
        int databaseSizeBeforeTest = imagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        // set the field null
        images.setGantry(null);

        // Create the Images, which fails.
        ImagesDTO imagesDTO = imagesMapper.toDto(images);

        restImagesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(imagesDTO)))
            .andExpect(status().isBadRequest());

        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkLaneIsRequired() throws Exception {
        int databaseSizeBeforeTest = imagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        // set the field null
        images.setLane(null);

        // Create the Images, which fails.
        ImagesDTO imagesDTO = imagesMapper.toDto(images);

        restImagesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(imagesDTO)))
            .andExpect(status().isBadRequest());

        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkVehicleIsRequired() throws Exception {
        int databaseSizeBeforeTest = imagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        // set the field null
        images.setVehicle(null);

        // Create the Images, which fails.
        ImagesDTO imagesDTO = imagesMapper.toDto(images);

        restImagesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(imagesDTO)))
            .andExpect(status().isBadRequest());

        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllImages() throws Exception {
        // Initialize the database
        imagesRepository.save(images);

        // Get all the imagesList
        restImagesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].guid").value(hasItem(DEFAULT_GUID)))
            .andExpect(jsonPath("$.[*].plate").value(hasItem(DEFAULT_PLATE)))
            .andExpect(jsonPath("$.[*].imageLpContentType").value(hasItem(DEFAULT_IMAGE_LP_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].imageLp").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGE_LP))))
            .andExpect(jsonPath("$.[*].imageThumbContentType").value(hasItem(DEFAULT_IMAGE_THUMB_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].imageThumb").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGE_THUMB))))
            .andExpect(jsonPath("$.[*].anpr").value(hasItem(DEFAULT_ANPR)))
            .andExpect(jsonPath("$.[*].rfid").value(hasItem(DEFAULT_RFID)))
            .andExpect(jsonPath("$.[*].dataStatus").value(hasItem(DEFAULT_DATA_STATUS)))
            .andExpect(jsonPath("$.[*].gantry").value(hasItem(DEFAULT_GANTRY.intValue())))
            .andExpect(jsonPath("$.[*].lane").value(hasItem(DEFAULT_LANE.intValue())))
            .andExpect(jsonPath("$.[*].kph").value(hasItem(DEFAULT_KPH.intValue())))
            .andExpect(jsonPath("$.[*].ambush").value(hasItem(DEFAULT_AMBUSH.intValue())))
            .andExpect(jsonPath("$.[*].direction").value(hasItem(DEFAULT_DIRECTION.intValue())))
            .andExpect(jsonPath("$.[*].vehicle").value(hasItem(DEFAULT_VEHICLE.intValue())))
            .andExpect(jsonPath("$.[*].issue").value(hasItem(DEFAULT_ISSUE)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)));
    }

    @Test
    void getImages() throws Exception {
        // Initialize the database
        imagesRepository.save(images);

        // Get the images
        restImagesMockMvc
            .perform(get(ENTITY_API_URL_ID, images.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.guid").value(DEFAULT_GUID))
            .andExpect(jsonPath("$.plate").value(DEFAULT_PLATE))
            .andExpect(jsonPath("$.imageLpContentType").value(DEFAULT_IMAGE_LP_CONTENT_TYPE))
            .andExpect(jsonPath("$.imageLp").value(Base64Utils.encodeToString(DEFAULT_IMAGE_LP)))
            .andExpect(jsonPath("$.imageThumbContentType").value(DEFAULT_IMAGE_THUMB_CONTENT_TYPE))
            .andExpect(jsonPath("$.imageThumb").value(Base64Utils.encodeToString(DEFAULT_IMAGE_THUMB)))
            .andExpect(jsonPath("$.anpr").value(DEFAULT_ANPR))
            .andExpect(jsonPath("$.rfid").value(DEFAULT_RFID))
            .andExpect(jsonPath("$.dataStatus").value(DEFAULT_DATA_STATUS))
            .andExpect(jsonPath("$.gantry").value(DEFAULT_GANTRY.intValue()))
            .andExpect(jsonPath("$.lane").value(DEFAULT_LANE.intValue()))
            .andExpect(jsonPath("$.kph").value(DEFAULT_KPH.intValue()))
            .andExpect(jsonPath("$.ambush").value(DEFAULT_AMBUSH.intValue()))
            .andExpect(jsonPath("$.direction").value(DEFAULT_DIRECTION.intValue()))
            .andExpect(jsonPath("$.vehicle").value(DEFAULT_VEHICLE.intValue()))
            .andExpect(jsonPath("$.issue").value(DEFAULT_ISSUE))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS));
    }

    @Test
    void getNonExistingImages() throws Exception {
        // Get the images
        restImagesMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingImages() throws Exception {
        // Initialize the database
        imagesRepository.save(images);

        int databaseSizeBeforeUpdate = imagesRepository.findAll().size();
        imagesSearchRepository.save(images);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(imagesSearchRepository.findAll());

        // Update the images
        Images updatedImages = imagesRepository.findById(images.getId()).get();
        updatedImages
            .guid(UPDATED_GUID)
            .plate(UPDATED_PLATE)
            .imageLp(UPDATED_IMAGE_LP)
            .imageLpContentType(UPDATED_IMAGE_LP_CONTENT_TYPE)
            .imageThumb(UPDATED_IMAGE_THUMB)
            .imageThumbContentType(UPDATED_IMAGE_THUMB_CONTENT_TYPE)
            .anpr(UPDATED_ANPR)
            .rfid(UPDATED_RFID)
            .dataStatus(UPDATED_DATA_STATUS)
            .gantry(UPDATED_GANTRY)
            .lane(UPDATED_LANE)
            .kph(UPDATED_KPH)
            .ambush(UPDATED_AMBUSH)
            .direction(UPDATED_DIRECTION)
            .vehicle(UPDATED_VEHICLE)
            .issue(UPDATED_ISSUE)
            .status(UPDATED_STATUS);
        ImagesDTO imagesDTO = imagesMapper.toDto(updatedImages);

        restImagesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, imagesDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(imagesDTO))
            )
            .andExpect(status().isOk());

        // Validate the Images in the database
        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeUpdate);
        Images testImages = imagesList.get(imagesList.size() - 1);
        assertThat(testImages.getGuid()).isEqualTo(UPDATED_GUID);
        assertThat(testImages.getPlate()).isEqualTo(UPDATED_PLATE);
        assertThat(testImages.getImageLp()).isEqualTo(UPDATED_IMAGE_LP);
        assertThat(testImages.getImageLpContentType()).isEqualTo(UPDATED_IMAGE_LP_CONTENT_TYPE);
        assertThat(testImages.getImageThumb()).isEqualTo(UPDATED_IMAGE_THUMB);
        assertThat(testImages.getImageThumbContentType()).isEqualTo(UPDATED_IMAGE_THUMB_CONTENT_TYPE);
        assertThat(testImages.getAnpr()).isEqualTo(UPDATED_ANPR);
        assertThat(testImages.getRfid()).isEqualTo(UPDATED_RFID);
        assertThat(testImages.getDataStatus()).isEqualTo(UPDATED_DATA_STATUS);
        assertThat(testImages.getGantry()).isEqualTo(UPDATED_GANTRY);
        assertThat(testImages.getLane()).isEqualTo(UPDATED_LANE);
        assertThat(testImages.getKph()).isEqualTo(UPDATED_KPH);
        assertThat(testImages.getAmbush()).isEqualTo(UPDATED_AMBUSH);
        assertThat(testImages.getDirection()).isEqualTo(UPDATED_DIRECTION);
        assertThat(testImages.getVehicle()).isEqualTo(UPDATED_VEHICLE);
        assertThat(testImages.getIssue()).isEqualTo(UPDATED_ISSUE);
        assertThat(testImages.getStatus()).isEqualTo(UPDATED_STATUS);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(imagesSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Images> imagesSearchList = IterableUtils.toList(imagesSearchRepository.findAll());
                Images testImagesSearch = imagesSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testImagesSearch.getGuid()).isEqualTo(UPDATED_GUID);
                assertThat(testImagesSearch.getPlate()).isEqualTo(UPDATED_PLATE);
                assertThat(testImagesSearch.getImageLp()).isEqualTo(UPDATED_IMAGE_LP);
                assertThat(testImagesSearch.getImageLpContentType()).isEqualTo(UPDATED_IMAGE_LP_CONTENT_TYPE);
                assertThat(testImagesSearch.getImageThumb()).isEqualTo(UPDATED_IMAGE_THUMB);
                assertThat(testImagesSearch.getImageThumbContentType()).isEqualTo(UPDATED_IMAGE_THUMB_CONTENT_TYPE);
                assertThat(testImagesSearch.getAnpr()).isEqualTo(UPDATED_ANPR);
                assertThat(testImagesSearch.getRfid()).isEqualTo(UPDATED_RFID);
                assertThat(testImagesSearch.getDataStatus()).isEqualTo(UPDATED_DATA_STATUS);
                assertThat(testImagesSearch.getGantry()).isEqualTo(UPDATED_GANTRY);
                assertThat(testImagesSearch.getLane()).isEqualTo(UPDATED_LANE);
                assertThat(testImagesSearch.getKph()).isEqualTo(UPDATED_KPH);
                assertThat(testImagesSearch.getAmbush()).isEqualTo(UPDATED_AMBUSH);
                assertThat(testImagesSearch.getDirection()).isEqualTo(UPDATED_DIRECTION);
                assertThat(testImagesSearch.getVehicle()).isEqualTo(UPDATED_VEHICLE);
                assertThat(testImagesSearch.getIssue()).isEqualTo(UPDATED_ISSUE);
                assertThat(testImagesSearch.getStatus()).isEqualTo(UPDATED_STATUS);
            });
    }

    @Test
    void putNonExistingImages() throws Exception {
        int databaseSizeBeforeUpdate = imagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        images.setId(count.incrementAndGet());

        // Create the Images
        ImagesDTO imagesDTO = imagesMapper.toDto(images);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restImagesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, imagesDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(imagesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Images in the database
        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchImages() throws Exception {
        int databaseSizeBeforeUpdate = imagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        images.setId(count.incrementAndGet());

        // Create the Images
        ImagesDTO imagesDTO = imagesMapper.toDto(images);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restImagesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(imagesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Images in the database
        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamImages() throws Exception {
        int databaseSizeBeforeUpdate = imagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        images.setId(count.incrementAndGet());

        // Create the Images
        ImagesDTO imagesDTO = imagesMapper.toDto(images);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restImagesMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(imagesDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Images in the database
        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateImagesWithPatch() throws Exception {
        // Initialize the database
        imagesRepository.save(images);

        int databaseSizeBeforeUpdate = imagesRepository.findAll().size();

        // Update the images using partial update
        Images partialUpdatedImages = new Images();
        partialUpdatedImages.setId(images.getId());

        partialUpdatedImages
            .plate(UPDATED_PLATE)
            .imageLp(UPDATED_IMAGE_LP)
            .imageLpContentType(UPDATED_IMAGE_LP_CONTENT_TYPE)
            .imageThumb(UPDATED_IMAGE_THUMB)
            .imageThumbContentType(UPDATED_IMAGE_THUMB_CONTENT_TYPE)
            .gantry(UPDATED_GANTRY)
            .kph(UPDATED_KPH)
            .ambush(UPDATED_AMBUSH)
            .direction(UPDATED_DIRECTION);

        restImagesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedImages.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedImages))
            )
            .andExpect(status().isOk());

        // Validate the Images in the database
        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeUpdate);
        Images testImages = imagesList.get(imagesList.size() - 1);
        assertThat(testImages.getGuid()).isEqualTo(DEFAULT_GUID);
        assertThat(testImages.getPlate()).isEqualTo(UPDATED_PLATE);
        assertThat(testImages.getImageLp()).isEqualTo(UPDATED_IMAGE_LP);
        assertThat(testImages.getImageLpContentType()).isEqualTo(UPDATED_IMAGE_LP_CONTENT_TYPE);
        assertThat(testImages.getImageThumb()).isEqualTo(UPDATED_IMAGE_THUMB);
        assertThat(testImages.getImageThumbContentType()).isEqualTo(UPDATED_IMAGE_THUMB_CONTENT_TYPE);
        assertThat(testImages.getAnpr()).isEqualTo(DEFAULT_ANPR);
        assertThat(testImages.getRfid()).isEqualTo(DEFAULT_RFID);
        assertThat(testImages.getDataStatus()).isEqualTo(DEFAULT_DATA_STATUS);
        assertThat(testImages.getGantry()).isEqualTo(UPDATED_GANTRY);
        assertThat(testImages.getLane()).isEqualTo(DEFAULT_LANE);
        assertThat(testImages.getKph()).isEqualTo(UPDATED_KPH);
        assertThat(testImages.getAmbush()).isEqualTo(UPDATED_AMBUSH);
        assertThat(testImages.getDirection()).isEqualTo(UPDATED_DIRECTION);
        assertThat(testImages.getVehicle()).isEqualTo(DEFAULT_VEHICLE);
        assertThat(testImages.getIssue()).isEqualTo(DEFAULT_ISSUE);
        assertThat(testImages.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    void fullUpdateImagesWithPatch() throws Exception {
        // Initialize the database
        imagesRepository.save(images);

        int databaseSizeBeforeUpdate = imagesRepository.findAll().size();

        // Update the images using partial update
        Images partialUpdatedImages = new Images();
        partialUpdatedImages.setId(images.getId());

        partialUpdatedImages
            .guid(UPDATED_GUID)
            .plate(UPDATED_PLATE)
            .imageLp(UPDATED_IMAGE_LP)
            .imageLpContentType(UPDATED_IMAGE_LP_CONTENT_TYPE)
            .imageThumb(UPDATED_IMAGE_THUMB)
            .imageThumbContentType(UPDATED_IMAGE_THUMB_CONTENT_TYPE)
            .anpr(UPDATED_ANPR)
            .rfid(UPDATED_RFID)
            .dataStatus(UPDATED_DATA_STATUS)
            .gantry(UPDATED_GANTRY)
            .lane(UPDATED_LANE)
            .kph(UPDATED_KPH)
            .ambush(UPDATED_AMBUSH)
            .direction(UPDATED_DIRECTION)
            .vehicle(UPDATED_VEHICLE)
            .issue(UPDATED_ISSUE)
            .status(UPDATED_STATUS);

        restImagesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedImages.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedImages))
            )
            .andExpect(status().isOk());

        // Validate the Images in the database
        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeUpdate);
        Images testImages = imagesList.get(imagesList.size() - 1);
        assertThat(testImages.getGuid()).isEqualTo(UPDATED_GUID);
        assertThat(testImages.getPlate()).isEqualTo(UPDATED_PLATE);
        assertThat(testImages.getImageLp()).isEqualTo(UPDATED_IMAGE_LP);
        assertThat(testImages.getImageLpContentType()).isEqualTo(UPDATED_IMAGE_LP_CONTENT_TYPE);
        assertThat(testImages.getImageThumb()).isEqualTo(UPDATED_IMAGE_THUMB);
        assertThat(testImages.getImageThumbContentType()).isEqualTo(UPDATED_IMAGE_THUMB_CONTENT_TYPE);
        assertThat(testImages.getAnpr()).isEqualTo(UPDATED_ANPR);
        assertThat(testImages.getRfid()).isEqualTo(UPDATED_RFID);
        assertThat(testImages.getDataStatus()).isEqualTo(UPDATED_DATA_STATUS);
        assertThat(testImages.getGantry()).isEqualTo(UPDATED_GANTRY);
        assertThat(testImages.getLane()).isEqualTo(UPDATED_LANE);
        assertThat(testImages.getKph()).isEqualTo(UPDATED_KPH);
        assertThat(testImages.getAmbush()).isEqualTo(UPDATED_AMBUSH);
        assertThat(testImages.getDirection()).isEqualTo(UPDATED_DIRECTION);
        assertThat(testImages.getVehicle()).isEqualTo(UPDATED_VEHICLE);
        assertThat(testImages.getIssue()).isEqualTo(UPDATED_ISSUE);
        assertThat(testImages.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    void patchNonExistingImages() throws Exception {
        int databaseSizeBeforeUpdate = imagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        images.setId(count.incrementAndGet());

        // Create the Images
        ImagesDTO imagesDTO = imagesMapper.toDto(images);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restImagesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, imagesDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(imagesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Images in the database
        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchImages() throws Exception {
        int databaseSizeBeforeUpdate = imagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        images.setId(count.incrementAndGet());

        // Create the Images
        ImagesDTO imagesDTO = imagesMapper.toDto(images);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restImagesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(imagesDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Images in the database
        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamImages() throws Exception {
        int databaseSizeBeforeUpdate = imagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        images.setId(count.incrementAndGet());

        // Create the Images
        ImagesDTO imagesDTO = imagesMapper.toDto(images);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restImagesMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(imagesDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Images in the database
        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteImages() throws Exception {
        // Initialize the database
        imagesRepository.save(images);
        imagesRepository.save(images);
        imagesSearchRepository.save(images);

        int databaseSizeBeforeDelete = imagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the images
        restImagesMockMvc
            .perform(delete(ENTITY_API_URL_ID, images.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Images> imagesList = imagesRepository.findAll();
        assertThat(imagesList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(imagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchImages() throws Exception {
        // Initialize the database
        images = imagesRepository.save(images);
        imagesSearchRepository.save(images);

        // Search the images
        restImagesMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + images.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].guid").value(hasItem(DEFAULT_GUID)))
            .andExpect(jsonPath("$.[*].plate").value(hasItem(DEFAULT_PLATE)))
            .andExpect(jsonPath("$.[*].imageLpContentType").value(hasItem(DEFAULT_IMAGE_LP_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].imageLp").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGE_LP))))
            .andExpect(jsonPath("$.[*].imageThumbContentType").value(hasItem(DEFAULT_IMAGE_THUMB_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].imageThumb").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGE_THUMB))))
            .andExpect(jsonPath("$.[*].anpr").value(hasItem(DEFAULT_ANPR)))
            .andExpect(jsonPath("$.[*].rfid").value(hasItem(DEFAULT_RFID)))
            .andExpect(jsonPath("$.[*].dataStatus").value(hasItem(DEFAULT_DATA_STATUS)))
            .andExpect(jsonPath("$.[*].gantry").value(hasItem(DEFAULT_GANTRY.intValue())))
            .andExpect(jsonPath("$.[*].lane").value(hasItem(DEFAULT_LANE.intValue())))
            .andExpect(jsonPath("$.[*].kph").value(hasItem(DEFAULT_KPH.intValue())))
            .andExpect(jsonPath("$.[*].ambush").value(hasItem(DEFAULT_AMBUSH.intValue())))
            .andExpect(jsonPath("$.[*].direction").value(hasItem(DEFAULT_DIRECTION.intValue())))
            .andExpect(jsonPath("$.[*].vehicle").value(hasItem(DEFAULT_VEHICLE.intValue())))
            .andExpect(jsonPath("$.[*].issue").value(hasItem(DEFAULT_ISSUE)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)));
    }
}
