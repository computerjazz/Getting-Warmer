package com.danielmerrill.gettingwarmer;

/**
 * Created by danielmerrill on 6/16/16.
 */

    //import javax.annotation.Generated;
    import com.google.gson.annotations.Expose;
    import com.google.gson.annotations.SerializedName;

    //@Generated("org.jsonschema2pojo")
    public class LoginModel {

        @SerializedName("status")
        @Expose
        private String status;
        @SerializedName("login")
        @Expose
        private String login;

        /**
         *
         * @return
         * The status
         */
        public String getStatus() {
            return status;
        }

        /**
         *
         * @param status
         * The status
         */
        public void setStatus(String status) {
            this.status = status;
        }

        /**
         *
         * @return
         * The login
         */
        public String getLogin() {
            return login;
        }

        /**
         *
         * @param login
         * The login
         */
        public void setLogin(String login) {
            this.login = login;
        }

    }

