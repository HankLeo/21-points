import React, { useEffect, useState } from 'react';
import InfiniteScroll from 'react-infinite-scroll-component';
import { Link, useLocation } from 'react-router-dom';
import { Button, Col, Form, FormGroup, Input, InputGroup, Row, Table } from 'reactstrap';
import { TextFormat, Translate, getPaginationState, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities, reset, searchEntities } from './points.reducer';

export const Points = () => {
    const dispatch = useAppDispatch();

    const pageLocation = useLocation();

    const [search, setSearch] = useState('');
    const [paginationState, setPaginationState] = useState(
        overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
    );
    const [sorting, setSorting] = useState(false);

    const pointsList = useAppSelector(state => state.points.entities);
    const loading = useAppSelector(state => state.points.loading);
    const links = useAppSelector(state => state.points.links);
    const updateSuccess = useAppSelector(state => state.points.updateSuccess);

    const getAllEntities = () => {
        if (search) {
            dispatch(
                searchEntities({
                    query: search,
                    page: paginationState.activePage - 1,
                    size: paginationState.itemsPerPage,
                    sort: `${paginationState.sort},${paginationState.order}`,
                }),
            );
        } else {
            dispatch(
                getEntities({
                    page: paginationState.activePage - 1,
                    size: paginationState.itemsPerPage,
                    sort: `${paginationState.sort},${paginationState.order}`,
                }),
            );
        }
    };

    const resetAll = () => {
        dispatch(reset());
        setPaginationState({
            ...paginationState,
            activePage: 1,
        });
        dispatch(getEntities({}));
    };

    useEffect(() => {
        resetAll();
    }, []);

    const startSearching = e => {
        if (search) {
            dispatch(reset());
            setPaginationState({
                ...paginationState,
                activePage: 1,
            });
            dispatch(
                searchEntities({
                    query: search,
                    page: paginationState.activePage - 1,
                    size: paginationState.itemsPerPage,
                    sort: `${paginationState.sort},${paginationState.order}`,
                }),
            );
        }
        e.preventDefault();
    };

    const clear = () => {
        dispatch(reset());
        setSearch('');
        setPaginationState({
            ...paginationState,
            activePage: 1,
        });
        dispatch(getEntities({}));
    };

    const handleSearch = event => setSearch(event.target.value);

    useEffect(() => {
        if (updateSuccess) {
            resetAll();
        }
    }, [updateSuccess]);

    useEffect(() => {
        getAllEntities();
    }, [paginationState.activePage]);

    const handleLoadMore = () => {
        if ((window as any).pageYOffset > 0) {
            setPaginationState({
                ...paginationState,
                activePage: paginationState.activePage + 1,
            });
        }
    };

    useEffect(() => {
        if (sorting) {
            getAllEntities();
            setSorting(false);
        }
    }, [sorting, search]);

    const sort = p => () => {
        dispatch(reset());
        setPaginationState({
            ...paginationState,
            activePage: 1,
            order: paginationState.order === ASC ? DESC : ASC,
            sort: p,
        });
        setSorting(true);
    };

    const handleSyncList = () => {
        resetAll();
    };

    const getSortIconByFieldName = (fieldName: string) => {
        const sortFieldName = paginationState.sort;
        const order = paginationState.order;
        if (sortFieldName !== fieldName) {
            return faSort;
        }
        return order === ASC ? faSortUp : faSortDown;
    };

    return (
        <div>
            <h2 id="points-heading" data-cy="PointsHeading">
                <Translate contentKey="twentyOnePointsApp.points.home.title">Points</Translate>
                <div className="d-flex justify-content-end">
                    <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
                        <FontAwesomeIcon icon="sync" spin={loading} />{' '}
                        <Translate contentKey="twentyOnePointsApp.points.home.refreshListLabel">Refresh List</Translate>
                    </Button>
                    <Link to="/points/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
                        <FontAwesomeIcon icon="plus" />
                        &nbsp;
                        <Translate contentKey="twentyOnePointsApp.points.home.createLabel">Create new Points</Translate>
                    </Link>
                </div>
            </h2>
            <Row>
                <Col sm="12">
                    <Form onSubmit={startSearching}>
                        <FormGroup>
                            <InputGroup>
                                <Input
                                    type="text"
                                    name="search"
                                    defaultValue={search}
                                    onChange={handleSearch}
                                    placeholder={translate('twentyOnePointsApp.points.home.search')}
                                />
                                <Button className="input-group-addon">
                                    <FontAwesomeIcon icon="search" />
                                </Button>
                                <Button type="reset" className="input-group-addon" onClick={clear}>
                                    <FontAwesomeIcon icon="trash" />
                                </Button>
                            </InputGroup>
                        </FormGroup>
                    </Form>
                </Col>
            </Row>
            <div className="table-responsive">
                <InfiniteScroll
                    dataLength={pointsList ? pointsList.length : 0}
                    next={handleLoadMore}
                    hasMore={paginationState.activePage - 1 < links.next}
                    loader={<div className="loader">Loading ...</div>}
                >
                    {pointsList && pointsList.length > 0 ? (
                        <Table responsive>
                            <thead>
                                <tr>
                                    <th className="hand" onClick={sort('id')}>
                                        <Translate contentKey="twentyOnePointsApp.points.id">ID</Translate>{' '}
                                        <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                                    </th>
                                    <th className="hand" onClick={sort('date')}>
                                        <Translate contentKey="twentyOnePointsApp.points.date">Date</Translate>{' '}
                                        <FontAwesomeIcon icon={getSortIconByFieldName('date')} />
                                    </th>
                                    <th className="hand" onClick={sort('exercise')}>
                                        <Translate contentKey="twentyOnePointsApp.points.exercise">Exercise</Translate>{' '}
                                        <FontAwesomeIcon icon={getSortIconByFieldName('exercise')} />
                                    </th>
                                    <th className="hand" onClick={sort('meals')}>
                                        <Translate contentKey="twentyOnePointsApp.points.meals">Meals</Translate>{' '}
                                        <FontAwesomeIcon icon={getSortIconByFieldName('meals')} />
                                    </th>
                                    <th className="hand" onClick={sort('alcohol')}>
                                        <Translate contentKey="twentyOnePointsApp.points.alcohol">Alcohol</Translate>{' '}
                                        <FontAwesomeIcon icon={getSortIconByFieldName('alcohol')} />
                                    </th>
                                    <th className="hand" onClick={sort('notes')}>
                                        <Translate contentKey="twentyOnePointsApp.points.notes">Notes</Translate>{' '}
                                        <FontAwesomeIcon icon={getSortIconByFieldName('notes')} />
                                    </th>
                                    <th>
                                        <Translate contentKey="twentyOnePointsApp.points.user">User</Translate>{' '}
                                        <FontAwesomeIcon icon="sort" />
                                    </th>
                                    <th />
                                </tr>
                            </thead>
                            <tbody>
                                {pointsList.map((points, i) => (
                                    <tr key={`entity-${i}`} data-cy="entityTable">
                                        <td>
                                            <Button tag={Link} to={`/points/${points.id}`} color="link" size="sm">
                                                {points.id}
                                            </Button>
                                        </td>
                                        <td>
                                            {points.date ? (
                                                <TextFormat type="date" value={points.date} format={APP_LOCAL_DATE_FORMAT} />
                                            ) : null}
                                        </td>
                                        <td>{points.exercise}</td>
                                        <td>{points.meals}</td>
                                        <td>{points.alcohol}</td>
                                        <td>{points.notes}</td>
                                        <td>{points.user ? points.user.login : ''}</td>
                                        <td className="text-end">
                                            <div className="btn-group flex-btn-group-container">
                                                <Button
                                                    tag={Link}
                                                    to={`/points/${points.id}`}
                                                    color="info"
                                                    size="sm"
                                                    data-cy="entityDetailsButton"
                                                >
                                                    <FontAwesomeIcon icon="eye" />{' '}
                                                    <span className="d-none d-md-inline">
                                                        <Translate contentKey="entity.action.view">View</Translate>
                                                    </span>
                                                </Button>
                                                <Button
                                                    tag={Link}
                                                    to={`/points/${points.id}/edit`}
                                                    color="primary"
                                                    size="sm"
                                                    data-cy="entityEditButton"
                                                >
                                                    <FontAwesomeIcon icon="pencil-alt" />{' '}
                                                    <span className="d-none d-md-inline">
                                                        <Translate contentKey="entity.action.edit">Edit</Translate>
                                                    </span>
                                                </Button>
                                                <Button
                                                    onClick={() => (window.location.href = `/points/${points.id}/delete`)}
                                                    color="danger"
                                                    size="sm"
                                                    data-cy="entityDeleteButton"
                                                >
                                                    <FontAwesomeIcon icon="trash" />{' '}
                                                    <span className="d-none d-md-inline">
                                                        <Translate contentKey="entity.action.delete">Delete</Translate>
                                                    </span>
                                                </Button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </Table>
                    ) : (
                        !loading && (
                            <div className="alert alert-warning">
                                <Translate contentKey="twentyOnePointsApp.points.home.notFound">No Points found</Translate>
                            </div>
                        )
                    )}
                </InfiniteScroll>
            </div>
        </div>
    );
};

export default Points;
