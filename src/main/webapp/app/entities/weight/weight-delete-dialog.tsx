import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Button, Modal, ModalBody, ModalFooter, ModalHeader } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { deleteEntity, getEntity } from './weight.reducer';

export const WeightDeleteDialog = () => {
    const dispatch = useAppDispatch();

    const pageLocation = useLocation();
    const navigate = useNavigate();
    const { id } = useParams<'id'>();

    const [loadModal, setLoadModal] = useState(false);

    useEffect(() => {
        dispatch(getEntity(id));
        setLoadModal(true);
    }, []);

    const weightEntity = useAppSelector(state => state.weight.entity);
    const updateSuccess = useAppSelector(state => state.weight.updateSuccess);

    const handleClose = () => {
        navigate('/weight');
    };

    useEffect(() => {
        if (updateSuccess && loadModal) {
            handleClose();
            setLoadModal(false);
        }
    }, [updateSuccess]);

    const confirmDelete = () => {
        dispatch(deleteEntity(weightEntity.id));
    };

    return (
        <Modal isOpen toggle={handleClose}>
            <ModalHeader toggle={handleClose} data-cy="weightDeleteDialogHeading">
                <Translate contentKey="entity.delete.title">Confirm delete operation</Translate>
            </ModalHeader>
            <ModalBody id="twentyOnePointsApp.weight.delete.question">
                <Translate contentKey="twentyOnePointsApp.weight.delete.question" interpolate={{ id: weightEntity.id }}>
                    Are you sure you want to delete this Weight?
                </Translate>
            </ModalBody>
            <ModalFooter>
                <Button color="secondary" onClick={handleClose}>
                    <FontAwesomeIcon icon="ban" />
                    &nbsp;
                    <Translate contentKey="entity.action.cancel">Cancel</Translate>
                </Button>
                <Button id="jhi-confirm-delete-weight" data-cy="entityConfirmDeleteButton" color="danger" onClick={confirmDelete}>
                    <FontAwesomeIcon icon="trash" />
                    &nbsp;
                    <Translate contentKey="entity.action.delete">Delete</Translate>
                </Button>
            </ModalFooter>
        </Modal>
    );
};

export default WeightDeleteDialog;
