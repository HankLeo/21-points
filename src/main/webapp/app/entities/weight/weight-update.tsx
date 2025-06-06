import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getUsers } from 'app/shared/reducers/user-management';
import { createEntity, getEntity, updateEntity } from './weight.reducer';

export const WeightUpdate = () => {
    const dispatch = useAppDispatch();

    const navigate = useNavigate();

    const { id } = useParams<'id'>();
    const isNew = id === undefined;

    const users = useAppSelector(state => state.userManagement.users);
    const weightEntity = useAppSelector(state => state.weight.entity);
    const loading = useAppSelector(state => state.weight.loading);
    const updating = useAppSelector(state => state.weight.updating);
    const updateSuccess = useAppSelector(state => state.weight.updateSuccess);

    const handleClose = () => {
        navigate('/weight');
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
        values.timestamp = convertDateTimeToServer(values.timestamp);
        if (values.weight !== undefined && typeof values.weight !== 'number') {
            values.weight = Number(values.weight);
        }

        const entity = {
            ...weightEntity,
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
            ? {
                  timestamp: displayDefaultDateTime(),
              }
            : {
                  ...weightEntity,
                  timestamp: convertDateTimeFromServer(weightEntity.timestamp),
                  user: weightEntity?.user?.id,
              };

    return (
        <div>
            <Row className="justify-content-center">
                <Col md="8">
                    <h2 id="twentyOnePointsApp.weight.home.createOrEditLabel" data-cy="WeightCreateUpdateHeading">
                        <Translate contentKey="twentyOnePointsApp.weight.home.createOrEditLabel">Create or edit a Weight</Translate>
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
                                    id="weight-id"
                                    label={translate('global.field.id')}
                                    validate={{ required: true }}
                                />
                            ) : null}
                            <ValidatedField
                                label={translate('twentyOnePointsApp.weight.timestamp')}
                                id="weight-timestamp"
                                name="timestamp"
                                data-cy="timestamp"
                                type="datetime-local"
                                placeholder="YYYY-MM-DD HH:mm"
                                validate={{
                                    required: { value: true, message: translate('entity.validation.required') },
                                }}
                            />
                            <ValidatedField
                                label={translate('twentyOnePointsApp.weight.weight')}
                                id="weight-weight"
                                name="weight"
                                data-cy="weight"
                                type="text"
                                validate={{
                                    required: { value: true, message: translate('entity.validation.required') },
                                    validate: v => isNumber(v) || translate('entity.validation.number'),
                                }}
                            />
                            <ValidatedField
                                id="weight-user"
                                name="user"
                                data-cy="user"
                                label={translate('twentyOnePointsApp.weight.user')}
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
                            <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/weight" replace color="info">
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

export default WeightUpdate;
