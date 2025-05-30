package io.hank.twentyonepoints.web.rest;

import static io.hank.twentyonepoints.domain.PointsAsserts.*;
import static io.hank.twentyonepoints.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hank.twentyonepoints.IntegrationTest;
import io.hank.twentyonepoints.domain.Points;
import io.hank.twentyonepoints.repository.PointsRepository;
import io.hank.twentyonepoints.repository.UserRepository;
import io.hank.twentyonepoints.repository.search.PointsSearchRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link PointsResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class PointsResourceIT {

    private static final LocalDate DEFAULT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final Integer DEFAULT_EXERCISE = 1;
    private static final Integer UPDATED_EXERCISE = 2;

    private static final Integer DEFAULT_MEALS = 1;
    private static final Integer UPDATED_MEALS = 2;

    private static final Integer DEFAULT_ALCOHOL = 1;
    private static final Integer UPDATED_ALCOHOL = 2;

    private static final String DEFAULT_NOTES = "AAAAAAAAAA";
    private static final String UPDATED_NOTES = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/points";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/points/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PointsRepository pointsRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private PointsRepository pointsRepositoryMock;

    @Autowired
    private PointsSearchRepository pointsSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPointsMockMvc;

    private Points points;

    private Points insertedPoints;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Points createEntity() {
        return new Points()
            .date(DEFAULT_DATE)
            .exercise(DEFAULT_EXERCISE)
            .meals(DEFAULT_MEALS)
            .alcohol(DEFAULT_ALCOHOL)
            .notes(DEFAULT_NOTES);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Points createUpdatedEntity() {
        return new Points()
            .date(UPDATED_DATE)
            .exercise(UPDATED_EXERCISE)
            .meals(UPDATED_MEALS)
            .alcohol(UPDATED_ALCOHOL)
            .notes(UPDATED_NOTES);
    }

    @BeforeEach
    public void initTest() {
        points = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedPoints != null) {
            pointsRepository.delete(insertedPoints);
            pointsSearchRepository.delete(insertedPoints);
            insertedPoints = null;
        }
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void createPoints() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        // Create the Points
        var returnedPoints = om.readValue(
            restPointsMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(points)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Points.class
        );

        // Validate the Points in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertPointsUpdatableFieldsEquals(returnedPoints, getPersistedPoints(returnedPoints));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(pointsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedPoints = returnedPoints;
    }

    @Test
    @Transactional
    void createPointsWithExistingId() throws Exception {
        // Create the Points with an existing ID
        points.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pointsSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restPointsMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(points)))
            .andExpect(status().isBadRequest());

        // Validate the Points in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        // set the field null
        points.setDate(null);

        // Create the Points, which fails.

        restPointsMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(points)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllPoints() throws Exception {
        // Initialize the database
        insertedPoints = pointsRepository.saveAndFlush(points);

        // Get all the pointsList
        restPointsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(points.getId().intValue())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].exercise").value(hasItem(DEFAULT_EXERCISE)))
            .andExpect(jsonPath("$.[*].meals").value(hasItem(DEFAULT_MEALS)))
            .andExpect(jsonPath("$.[*].alcohol").value(hasItem(DEFAULT_ALCOHOL)))
            .andExpect(jsonPath("$.[*].notes").value(hasItem(DEFAULT_NOTES)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPointsWithEagerRelationshipsIsEnabled() throws Exception {
        when(pointsRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPointsMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(pointsRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPointsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(pointsRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPointsMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(pointsRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getPoints() throws Exception {
        // Initialize the database
        insertedPoints = pointsRepository.saveAndFlush(points);

        // Get the points
        restPointsMockMvc
            .perform(get(ENTITY_API_URL_ID, points.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(points.getId().intValue()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
            .andExpect(jsonPath("$.exercise").value(DEFAULT_EXERCISE))
            .andExpect(jsonPath("$.meals").value(DEFAULT_MEALS))
            .andExpect(jsonPath("$.alcohol").value(DEFAULT_ALCOHOL))
            .andExpect(jsonPath("$.notes").value(DEFAULT_NOTES));
    }

    @Test
    @Transactional
    void getNonExistingPoints() throws Exception {
        // Get the points
        restPointsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPoints() throws Exception {
        // Initialize the database
        insertedPoints = pointsRepository.saveAndFlush(points);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        pointsSearchRepository.save(points);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pointsSearchRepository.findAll());

        // Update the points
        Points updatedPoints = pointsRepository.findById(points.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPoints are not directly saved in db
        em.detach(updatedPoints);
        updatedPoints.date(UPDATED_DATE).exercise(UPDATED_EXERCISE).meals(UPDATED_MEALS).alcohol(UPDATED_ALCOHOL).notes(UPDATED_NOTES);

        restPointsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedPoints.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedPoints))
            )
            .andExpect(status().isOk());

        // Validate the Points in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPointsToMatchAllProperties(updatedPoints);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(pointsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Points> pointsSearchList = Streamable.of(pointsSearchRepository.findAll()).toList();
                Points testPointsSearch = pointsSearchList.get(searchDatabaseSizeAfter - 1);

                assertPointsAllPropertiesEquals(testPointsSearch, updatedPoints);
            });
    }

    @Test
    @Transactional
    void putNonExistingPoints() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        points.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPointsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, points.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(points))
            )
            .andExpect(status().isBadRequest());

        // Validate the Points in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchPoints() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        points.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPointsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(points))
            )
            .andExpect(status().isBadRequest());

        // Validate the Points in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPoints() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        points.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPointsMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(points)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Points in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdatePointsWithPatch() throws Exception {
        // Initialize the database
        insertedPoints = pointsRepository.saveAndFlush(points);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the points using partial update
        Points partialUpdatedPoints = new Points();
        partialUpdatedPoints.setId(points.getId());

        partialUpdatedPoints.exercise(UPDATED_EXERCISE).alcohol(UPDATED_ALCOHOL);

        restPointsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPoints.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPoints))
            )
            .andExpect(status().isOk());

        // Validate the Points in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPointsUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPoints, points), getPersistedPoints(points));
    }

    @Test
    @Transactional
    void fullUpdatePointsWithPatch() throws Exception {
        // Initialize the database
        insertedPoints = pointsRepository.saveAndFlush(points);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the points using partial update
        Points partialUpdatedPoints = new Points();
        partialUpdatedPoints.setId(points.getId());

        partialUpdatedPoints
            .date(UPDATED_DATE)
            .exercise(UPDATED_EXERCISE)
            .meals(UPDATED_MEALS)
            .alcohol(UPDATED_ALCOHOL)
            .notes(UPDATED_NOTES);

        restPointsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPoints.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPoints))
            )
            .andExpect(status().isOk());

        // Validate the Points in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPointsUpdatableFieldsEquals(partialUpdatedPoints, getPersistedPoints(partialUpdatedPoints));
    }

    @Test
    @Transactional
    void patchNonExistingPoints() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        points.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPointsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, points.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(points))
            )
            .andExpect(status().isBadRequest());

        // Validate the Points in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPoints() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        points.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPointsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(points))
            )
            .andExpect(status().isBadRequest());

        // Validate the Points in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPoints() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        points.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPointsMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(points)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Points in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deletePoints() throws Exception {
        // Initialize the database
        insertedPoints = pointsRepository.saveAndFlush(points);
        pointsRepository.save(points);
        pointsSearchRepository.save(points);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the points
        restPointsMockMvc
            .perform(delete(ENTITY_API_URL_ID, points.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pointsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchPoints() throws Exception {
        // Initialize the database
        insertedPoints = pointsRepository.saveAndFlush(points);
        pointsSearchRepository.save(points);

        // Search the points
        restPointsMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + points.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(points.getId().intValue())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].exercise").value(hasItem(DEFAULT_EXERCISE)))
            .andExpect(jsonPath("$.[*].meals").value(hasItem(DEFAULT_MEALS)))
            .andExpect(jsonPath("$.[*].alcohol").value(hasItem(DEFAULT_ALCOHOL)))
            .andExpect(jsonPath("$.[*].notes").value(hasItem(DEFAULT_NOTES)));
    }

    protected long getRepositoryCount() {
        return pointsRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Points getPersistedPoints(Points points) {
        return pointsRepository.findById(points.getId()).orElseThrow();
    }

    protected void assertPersistedPointsToMatchAllProperties(Points expectedPoints) {
        assertPointsAllPropertiesEquals(expectedPoints, getPersistedPoints(expectedPoints));
    }

    protected void assertPersistedPointsToMatchUpdatableProperties(Points expectedPoints) {
        assertPointsAllUpdatablePropertiesEquals(expectedPoints, getPersistedPoints(expectedPoints));
    }
}
