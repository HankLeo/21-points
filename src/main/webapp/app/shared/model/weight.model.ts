import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';

export interface IWeight {
    id?: number;
    timestamp?: dayjs.Dayjs;
    weight?: number;
    user?: IUser | null;
}

export const defaultValue: Readonly<IWeight> = {};
