package io.hank.twentyonepoints.web.rest;

import io.hank.twentyonepoints.domain.Points;
import io.hank.twentyonepoints.repository.PointsRepository;
import io.hank.twentyonepoints.repository.search.PointsSearchRepository;
import io.hank.twentyonepoints.web.rest.errors.BadRequestAlertException;
import io.hank.twentyonepoints.web.rest.errors.ElasticsearchExceptionMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link io.hank.twentyonepoints.domain.Points}.
 */
@RestController
@RequestMapping("/api/points")
@Transactional
public class PointsResource {

    private static final Logger LOG = LoggerFactory.getLogger(PointsResource.class);

    private static final String ENTITY_NAME = "points";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PointsRepository pointsRepository;

    private final PointsSearchRepository pointsSearchRepository;

    public PointsResource(PointsRepository pointsRepository, PointsSearchRepository pointsSearchRepository) {
        this.pointsRepository = pointsRepository;
        this.pointsSearchRepository = pointsSearchRepository;
    }

    /**
     * {@code POST  /points} : Create a new points.
     *
     * @param points the points to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new points, or with status {@code 400 (Bad Request)} if the points has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Points> createPoints(@Valid @RequestBody Points points) throws URISyntaxException {
        LOG.debug("REST request to save Points : {}", points);
        if (points.getId() != null) {
            throw new BadRequestAlertException("A new points cannot already have an ID", ENTITY_NAME, "idexists");
        }
        points = pointsRepository.save(points);
        pointsSearchRepository.index(points);
        return ResponseEntity.created(new URI("/api/points/" + points.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, points.getId().toString()))
            .body(points);
    }

    /**
     * {@code PUT  /points/:id} : Updates an existing points.
     *
     * @param id the id of the points to save.
     * @param points the points to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated points,
     * or with status {@code 400 (Bad Request)} if the points is not valid,
     * or with status {@code 500 (Internal Server Error)} if the points couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Points> updatePoints(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Points points
    ) throws URISyntaxException {
        LOG.debug("REST request to update Points : {}, {}", id, points);
        if (points.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, points.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!pointsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        points = pointsRepository.save(points);
        pointsSearchRepository.index(points);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, points.getId().toString()))
            .body(points);
    }

    /**
     * {@code PATCH  /points/:id} : Partial updates given fields of an existing points, field will ignore if it is null
     *
     * @param id the id of the points to save.
     * @param points the points to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated points,
     * or with status {@code 400 (Bad Request)} if the points is not valid,
     * or with status {@code 404 (Not Found)} if the points is not found,
     * or with status {@code 500 (Internal Server Error)} if the points couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Points> partialUpdatePoints(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Points points
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Points partially : {}, {}", id, points);
        if (points.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, points.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!pointsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Points> result = pointsRepository
            .findById(points.getId())
            .map(existingPoints -> {
                if (points.getDate() != null) {
                    existingPoints.setDate(points.getDate());
                }
                if (points.getExercise() != null) {
                    existingPoints.setExercise(points.getExercise());
                }
                if (points.getMeals() != null) {
                    existingPoints.setMeals(points.getMeals());
                }
                if (points.getAlcohol() != null) {
                    existingPoints.setAlcohol(points.getAlcohol());
                }
                if (points.getNotes() != null) {
                    existingPoints.setNotes(points.getNotes());
                }

                return existingPoints;
            })
            .map(pointsRepository::save)
            .map(savedPoints -> {
                pointsSearchRepository.index(savedPoints);
                return savedPoints;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, points.getId().toString())
        );
    }

    /**
     * {@code GET  /points} : get all the points.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of points in body.
     */
    @GetMapping("")
    public ResponseEntity<List<Points>> getAllPoints(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Points");
        Page<Points> page;
        if (eagerload) {
            page = pointsRepository.findAllWithEagerRelationships(pageable);
        } else {
            page = pointsRepository.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /points/:id} : get the "id" points.
     *
     * @param id the id of the points to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the points, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Points> getPoints(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Points : {}", id);
        Optional<Points> points = pointsRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(points);
    }

    /**
     * {@code DELETE  /points/:id} : delete the "id" points.
     *
     * @param id the id of the points to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePoints(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Points : {}", id);
        pointsRepository.deleteById(id);
        pointsSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /points/_search?query=:query} : search for the points corresponding
     * to the query.
     *
     * @param query the query of the points search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public ResponseEntity<List<Points>> searchPoints(
        @RequestParam("query") String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to search for a page of Points for query {}", query);
        try {
            Page<Points> page = pointsSearchRepository.search(query, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
