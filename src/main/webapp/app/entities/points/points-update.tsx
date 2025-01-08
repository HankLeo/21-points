import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getUsers } from 'app/shared/reducers/user-management';
import { createEntity, getEntity, updateEntity } from './points.reducer';

export const PointsUpdate = () => {
    const dispatch = useAppDispatch();

    const navigate = useNavigate();

    const { id } = useParams<'id'>();
    const isNew = id === undefined;

    const users = useAppSelector(state => state.userManagement.users);
    const pointsEntity = useAppSelector(state => state.points.entity);
    const loading = useAppSelector(state => state.points.loading);
    const updating = useAppSelector(state => state.points.updating);
    const updateSuccess = useAppSelector(state => state.points.updateSuccess);

    const handleClose = () => {
        navigate('/points');
    };

    useEffect(() => {
        if (!isNew) {
            dispatch(getEntity(id));
        }

        dispatch(getUsers({}));
    }, []);

    useEffect(() => {
        if (updateSuccess) {
            handleClose();
        }
    }, [updateSuccess]);

    const saveEntity = values => {
        if (values.id !== undefined && typeof values.id !== 'number') {
            values.id = Number(values.id);
        }
        if (values.exercise !== undefined && typeof values.exercise !== 'number') {
            values.exercise = Number(values.exercise);
        }
        if (values.meals !== undefined && typeof values.meals !== 'number') {
            values.meals = Number(values.meals);
        }
        if (values.alcohol !== undefined && typeof values.alcohol !== 'number') {
            values.alcohol = Number(values.alcohol);
        }

        const entity = {
            ...pointsEntity,
            ...values,
            user: users.find(it => it.id.toString() === values.user?.toString()),
        };

        if (isNew) {
            dispatch(createEntity(entity));
        } else {
            dispatch(updateEntity(entity));
        }
    };

    const defaultValues = () =>
        isNew
            ? {}
            : {
                  ...pointsEntity,
                  user: pointsEntity?.user?.id,
              };

    return (
        <div>
            <Row className="justify-content-center">
                <Col md="8">
                    <h2 id="twentyOnePointsApp.points.home.createOrEditLabel" data-cy="PointsCreateUpdateHeading">
                        <Translate contentKey="twentyOnePointsApp.points.home.createOrEditLabel">Create or edit a Points</Translate>
                    </h2>
                </Col>
            </Row>
            <Row className="justify-content-center">
                <Col md="8">
                    {loading ? (
                        <p>Loading...</p>
                    ) : (
                        <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
                            {!isNew ? (
                                <ValidatedField
                                    name="id"
                                    required
                                    readOnly
                                    id="points-id"
                                    label={translate('global.field.id')}
                                    validate={{ required: true }}
                                />
                            ) : null}
                            <ValidatedField
                                label={translate('twentyOnePointsApp.points.date')}
                                id="points-date"
                                name="date"
                                data-cy="date"
                                type="date"
                                validate={{
                                    required: { value: true, message: translate('entity.validation.required') },
                                }}
                            />
                            <ValidatedField
                                label={translate('twentyOnePointsApp.points.exercise')}
                                id="points-exercise"
                                name="exercise"
                                data-cy="exercise"
                                type="text"
                            />
                            <ValidatedField
                                label={translate('twentyOnePointsApp.points.meals')}
                                id="points-meals"
                                name="meals"
                                data-cy="meals"
                                type="text"
                            />
                            <ValidatedField
                                label={translate('twentyOnePointsApp.points.alcohol')}
                                id="points-alcohol"
                                name="alcohol"
                                data-cy="alcohol"
                                type="text"
                            />
                            <ValidatedField
                                label={translate('twentyOnePointsApp.points.notes')}
                                id="points-notes"
                                name="notes"
                                data-cy="notes"
                                type="text"
                                validate={{
                                    maxLength: { value: 140, message: translate('entity.validation.maxlength', { max: 140 }) },
                                }}
                            />
                            <ValidatedField
                                id="points-user"
                                name="user"
                                data-cy="user"
                                label={translate('twentyOnePointsApp.points.user')}
                                type="select"
                            >
                                <option value="" key="0" />
                                {users
                                    ? users.map(otherEntity => (
                                          <option value={otherEntity.id} key={otherEntity.id}>
                                              {otherEntity.login}
                                          </option>
                                      ))
                                    : null}
                            </ValidatedField>
                            <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/points" replace color="info">
                                <FontAwesomeIcon icon="arrow-left" />
                                &nbsp;
                                <span className="d-none d-md-inline">
                                    <Translate contentKey="entity.action.back">Back</Translate>
                                </span>
                            </Button>
                            &nbsp;
                            <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                                <FontAwesomeIcon icon="save" />
                                &nbsp;
                                <Translate contentKey="entity.action.save">Save</Translate>
                            </Button>
                        </ValidatedForm>
                    )}
                </Col>
            </Row>
        </div>
    );
};

export default PointsUpdate;
