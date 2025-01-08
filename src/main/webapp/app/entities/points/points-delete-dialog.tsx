import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Button, Modal, ModalBody, ModalFooter, ModalHeader } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { deleteEntity, getEntity } from './points.reducer';

export const PointsDeleteDialog = () => {
    const dispatch = useAppDispatch();

    const pageLocation = useLocation();
    const navigate = useNavigate();
    const { id } = useParams<'id'>();

    const [loadModal, setLoadModal] = useState(false);

    useEffect(() => {
        dispatch(getEntity(id));
        setLoadModal(true);
    }, []);

    const pointsEntity = useAppSelector(state => state.points.entity);
    const updateSuccess = useAppSelector(state => state.points.updateSuccess);

    const handleClose = () => {
        navigate('/points');
    };

    useEffect(() => {
        if (updateSuccess && loadModal) {
            handleClose();
            setLoadModal(false);
        }
    }, [updateSuccess]);

    const confirmDelete = () => {
        dispatch(deleteEntity(pointsEntity.id));
    };

    return (
        <Modal isOpen toggle={handleClose}>
            <ModalHeader toggle={handleClose} data-cy="pointsDeleteDialogHeading">
                <Translate contentKey="entity.delete.title">Confirm delete operation</Translate>
            </ModalHeader>
            <ModalBody id="twentyOnePointsApp.points.delete.question">
                <Translate contentKey="twentyOnePointsApp.points.delete.question" interpolate={{ id: pointsEntity.id }}>
                    Are you sure you want to delete this Points?
                </Translate>
            </ModalBody>
            <ModalFooter>
                <Button color="secondary" onClick={handleClose}>
                    <FontAwesomeIcon icon="ban" />
                    &nbsp;
                    <Translate contentKey="entity.action.cancel">Cancel</Translate>
                </Button>
                <Button id="jhi-confirm-delete-points" data-cy="entityConfirmDeleteButton" color="danger" onClick={confirmDelete}>
                    <FontAwesomeIcon icon="trash" />
                    &nbsp;
                    <Translate contentKey="entity.action.delete">Delete</Translate>
                </Button>
            </ModalFooter>
        </Modal>
    );
};

export default PointsDeleteDialog;
