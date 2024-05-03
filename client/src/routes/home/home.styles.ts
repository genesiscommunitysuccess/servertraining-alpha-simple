import { css } from '@microsoft/fast-element';

export const HomeStyles = css`
  :host {
    /* insert css styles here */
  }
    .top-layout {
        height: 45%;
        flex-direction: row;
    }

    .positions-card {
        flex: 1;
        margin-top: calc(var(--design-unit) * 3px);
    }

    .card-title {
        padding: calc(var(--design-unit) * 3px);
        background-color: #22272a;
        font-size: 13px;
        font-weight: bold;
    }
`;
