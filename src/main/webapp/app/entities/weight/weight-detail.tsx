import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './weight.reducer';

export const WeightDetail = () => {
    const dispatch = useAppDispatch();

    const { id } = useParams<'id'>();

    useEffect(() => {
        dispatch(getEntity(id));
    }, []);

    const weightEntity = useAppSelector(state => state.weight.entity);
    return (
        <Row>
            <Col md="8">
                <h2 data-cy="weightDetailsHeading">
                    <Translate contentKey="twentyOnePointsApp.weight.detail.title">Weight</Translate>
                </h2>
                <dl className="jh-entity-details">
                    <dt>
                        <span id="id">
                            <Translate contentKey="global.field.id">ID</Translate>
                        </span>
                    </dt>
                    <dd>{weightEntity.id}</dd>
                    <dt>
                        <span id="timestamp">
                            <Translate contentKey="twentyOnePointsApp.weight.timestamp">Timestamp</Translate>
                        </span>
                    </dt>
                    <dd>
                        {weightEntity.timestamp ? <TextFormat value={weightEntity.timestamp} type="date" format={APP_DATE_FORMAT} /> : null}
                    </dd>
                    <dt>
                        <span id="weight">
                            <Translate contentKey="twentyOnePointsApp.weight.weight">Weight</Translate>
                        </span>
                    </dt>
                    <dd>{weightEntity.weight}</dd>
                    <dt>
                        <Translate contentKey="twentyOnePointsApp.weight.user">User</Translate>
                    </dt>
                    <dd>{weightEntity.user ? weightEntity.user.login : ''}</dd>
                </dl>
                <Button tag={Link} to="/weight" replace color="info" data-cy="entityDetailsBackButton">
                    <FontAwesomeIcon icon="arrow-left" />{' '}
                    <span className="d-none d-md-inline">
                        <Translate contentKey="entity.action.back">Back</Translate>
                    </span>
                </Button>
                &nbsp;
                <Button tag={Link} to={`/weight/${weightEntity.id}/edit`} replace color="primary">
                    <FontAwesomeIcon icon="pencil-alt" />{' '}
                    <span className="d-none d-md-inline">
                        <Translate contentKey="entity.action.edit">Edit</Translate>
                    </span>
                </Button>
            </Col>
        </Row>
    );
};

export default WeightDetail;
