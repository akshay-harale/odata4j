package org.odata4j.test.integration.producer.jpa.medialinkentity;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * The Class MediaResourceEntity entity with media content.
 */
@Entity
@Table(name = "MediaResourceEntity")
public class MediaResourceEntity implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Basic(optional = false)
  @Column(name = "MediaResourceID")
  private Integer MediaResourceID;

  @Lob
  @Basic(fetch = FetchType.LAZY)
  @Column(name = "MediaResource")
  private byte[] MediaResource;

  @Column(name = "Description")
  private String Description;

  /**
   * Gets the media resource id.
   *
   * @return the mediaResourceID
   */
  public Integer getMediaResourceID() {
    return MediaResourceID;
  }

  /**
   * Sets the media resource id.
   *
   * @param mediaResourceID the mediaResourceID to set
   */
  public void setMediaResourceID(Integer mediaResourceID) {
    MediaResourceID = mediaResourceID;
  }

  /**
   * Gets the media resource.
   *
   * @return the mediaResource
   */
  public byte[] getMediaResource() {
    return MediaResource;
  }

  /**
   * Sets the media resource.
   *
   * @param mediaResource the mediaResource to set
   */
  public void setMediaResource(byte[] mediaResource) {
    MediaResource = mediaResource;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return Description;
  }

  /**
   * Sets the description.
   *
   * @param description the description to set
   */
  public void setDescription(String description) {
    Description = description;
  }

  /**
   * Gets the serialversionuid.
   *
   * @return the serialversionuid
   */
  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  @Override
  public String toString() {
    return "org.odata4j.test.integration.producer.jpa.medialinkentity[MediaResourceEntity=" + MediaResourceID + "]";
  }

}
