import axios from 'axios';
import { createAsyncThunk, isFulfilled, isPending } from '@reduxjs/toolkit';
import { loadMoreDataWhenScrolled, parseHeaderForLinks } from 'react-jhipster';
import { cleanEntity } from 'app/shared/util/entity-utils';
import { EntityState, IQueryParams, createEntitySlice, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { IPoints, defaultValue } from 'app/shared/model/points.model';

const initialState: EntityState<IPoints> = {
    loading: false,
    errorMessage: null,
    entities: [],
    entity: defaultValue,
    links: { next: 0 },
    updating: false,
    totalItems: 0,
    updateSuccess: false,
};

const apiUrl = 'api/points';
const apiSearchUrl = 'api/points/_search';

// Actions

export const searchEntities = createAsyncThunk(
    'points/search_entity',
    async ({ query, page, size, sort }: IQueryParams) => {
        const requestUrl = `${apiSearchUrl}?query=${query}${sort ? `&page=${page}&size=${size}&sort=${sort}` : ''}`;
        return axios.get<IPoints[]>(requestUrl);
    },
    { serializeError: serializeAxiosError },
);

export const getEntities = createAsyncThunk(
    'points/fetch_entity_list',
    async ({ page, size, sort }: IQueryParams) => {
        const requestUrl = `${apiUrl}?${sort ? `page=${page}&size=${size}&sort=${sort}&` : ''}cacheBuster=${new Date().getTime()}`;
        return axios.get<IPoints[]>(requestUrl);
    },
    { serializeError: serializeAxiosError },
);

export const getEntity = createAsyncThunk(
    'points/fetch_entity',
    async (id: string | number) => {
        const requestUrl = `${apiUrl}/${id}`;
        return axios.get<IPoints>(requestUrl);
    },
    { serializeError: serializeAxiosError },
);

export const createEntity = createAsyncThunk(
    'points/create_entity',
    async (entity: IPoints, thunkAPI) => {
        return axios.post<IPoints>(apiUrl, cleanEntity(entity));
    },
    { serializeError: serializeAxiosError },
);

export const updateEntity = createAsyncThunk(
    'points/update_entity',
    async (entity: IPoints, thunkAPI) => {
        return axios.put<IPoints>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    },
    { serializeError: serializeAxiosError },
);

export const partialUpdateEntity = createAsyncThunk(
    'points/partial_update_entity',
    async (entity: IPoints, thunkAPI) => {
        return axios.patch<IPoints>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    },
    { serializeError: serializeAxiosError },
);

export const deleteEntity = createAsyncThunk(
    'points/delete_entity',
    async (id: string | number, thunkAPI) => {
        const requestUrl = `${apiUrl}/${id}`;
        return await axios.delete<IPoints>(requestUrl);
    },
    { serializeError: serializeAxiosError },
);

// slice

export const PointsSlice = createEntitySlice({
    name: 'points',
    initialState,
    extraReducers(builder) {
        builder
            .addCase(getEntity.fulfilled, (state, action) => {
                state.loading = false;
                state.entity = action.payload.data;
            })
            .addCase(deleteEntity.fulfilled, state => {
                state.updating = false;
                state.updateSuccess = true;
                state.entity = {};
            })
            .addMatcher(isFulfilled(getEntities, searchEntities), (state, action) => {
                const { data, headers } = action.payload;
                const links = parseHeaderForLinks(headers.link);

                return {
                    ...state,
                    loading: false,
                    links,
                    entities: loadMoreDataWhenScrolled(state.entities, data, links),
                    totalItems: parseInt(headers['x-total-count'], 10),
                };
            })
            .addMatcher(isFulfilled(createEntity, updateEntity, partialUpdateEntity), (state, action) => {
                state.updating = false;
                state.loading = false;
                state.updateSuccess = true;
                state.entity = action.payload.data;
            })
            .addMatcher(isPending(getEntities, getEntity, searchEntities), state => {
                state.errorMessage = null;
                state.updateSuccess = false;
                state.loading = true;
            })
            .addMatcher(isPending(createEntity, updateEntity, partialUpdateEntity, deleteEntity), state => {
                state.errorMessage = null;
                state.updateSuccess = false;
                state.updating = true;
            });
    },
});

export const { reset } = PointsSlice.actions;

// Reducer
export default PointsSlice.reducer;
