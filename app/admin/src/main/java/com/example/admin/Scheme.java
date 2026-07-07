package com.example.admin;

public class Scheme {
    private int id;
    private String scheme_id;
    private String scheme_name;
    private String scheme_category;
    private String department_name;
    private String description;
    private String benefit_type;
    private String benefit_amount;
    private String application_start_date;
    private String application_end_date;
    private String required_documents;
    private String min_income;
    private String max_income;
    private int min_age;
    private int max_age;
    private String gender_restriction;
    private String caste_restriction;
    private String scheme_status;
    private String portal_url;  // NEW FIELD

    public Scheme() {}

    // Getters
    public int getId() { return id; }
    public String getScheme_id() { return scheme_id; }
    public String getScheme_name() { return scheme_name; }
    public String getScheme_category() { return scheme_category != null ? scheme_category : ""; }
    public String getDepartment_name() { return department_name != null ? department_name : ""; }
    public String getDescription() { return description != null ? description : ""; }
    public String getBenefit_type() { return benefit_type != null ? benefit_type : ""; }
    public String getBenefit_amount() { return benefit_amount != null ? benefit_amount : ""; }
    public String getApplication_start_date() { return application_start_date != null ? application_start_date : ""; }
    public String getApplication_end_date() { return application_end_date != null ? application_end_date : ""; }
    public String getRequired_documents() { return required_documents != null ? required_documents : ""; }
    public String getMin_income() { return min_income != null ? min_income : ""; }
    public String getMax_income() { return max_income != null ? max_income : ""; }
    public int getMin_age() { return min_age; }
    public int getMax_age() { return max_age; }
    public String getGender_restriction() { return gender_restriction != null ? gender_restriction : "Any"; }
    public String getCaste_restriction() { return caste_restriction != null ? caste_restriction : "All"; }
    public String getScheme_status() { return scheme_status != null ? scheme_status : "Active"; }
    public String getPortal_url() { return portal_url != null ? portal_url : ""; }  // NEW GETTER

    // Setters
    public void setId(int id) { this.id = id; }
    public void setScheme_id(String scheme_id) { this.scheme_id = scheme_id; }
    public void setScheme_name(String scheme_name) { this.scheme_name = scheme_name; }
    public void setScheme_category(String scheme_category) { this.scheme_category = scheme_category; }
    public void setDepartment_name(String department_name) { this.department_name = department_name; }
    public void setDescription(String description) { this.description = description; }
    public void setBenefit_type(String benefit_type) { this.benefit_type = benefit_type; }
    public void setBenefit_amount(String benefit_amount) { this.benefit_amount = benefit_amount; }
    public void setApplication_start_date(String application_start_date) { this.application_start_date = application_start_date; }
    public void setApplication_end_date(String application_end_date) { this.application_end_date = application_end_date; }
    public void setRequired_documents(String required_documents) { this.required_documents = required_documents; }
    public void setMin_income(String min_income) { this.min_income = min_income; }
    public void setMax_income(String max_income) { this.max_income = max_income; }
    public void setMin_age(int min_age) { this.min_age = min_age; }
    public void setMax_age(int max_age) { this.max_age = max_age; }
    public void setGender_restriction(String gender_restriction) { this.gender_restriction = gender_restriction; }
    public void setCaste_restriction(String caste_restriction) { this.caste_restriction = caste_restriction; }
    public void setScheme_status(String scheme_status) { this.scheme_status = scheme_status; }
    public void setPortal_url(String portal_url) { this.portal_url = portal_url; }  // NEW SETTER
}